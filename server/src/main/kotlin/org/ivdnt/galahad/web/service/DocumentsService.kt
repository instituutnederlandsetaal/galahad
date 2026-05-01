package org.ivdnt.galahad.web.service

import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.io.path.createTempDirectory
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.documents.Document
import org.ivdnt.galahad.documents.DocumentMetadata
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.exceptions.FileUploadException
import org.ivdnt.galahad.util.ThreadPoolUtil
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

val ZIP_TYPES: List<String> =
    listOf("application/zip", "application/x-zip-compressed", "application/octet-stream")

@Service
class DocumentsService(val corpora: CorporaService) : Logging {

    fun readOrThrow(corpus: UUID, document: String, user: User): Document =
        corpora.readOrThrow(corpus, user).documents.readOrThrow(document)

    fun readAll(corpus: UUID, user: User): List<DocumentMetadata> =
        corpora.readOrThrow(corpus, user).documents.readAll().map { it.metadata }

    fun createOrThrow(file: MultipartFile, corpus: UUID, user: User) {
        if (file.contentType in ZIP_TYPES) {
            uploadZipFile(file, corpus, user)
        } else {
            createDocumentWithSourceLayer(corpus, user, file.originalFilename!!, file.inputStream)
        }
    }

    fun deleteOrThrow(corpus: UUID, document: String, user: User) {
        // Delete all jobs and results of this document.
        corpora.readWriteOrThrow(corpus, user).jobs.readAll().forEach {
            it.results.deleteOrNull(document)
        } // Doesn't matter if null.
        // Now delete it as write access
        val docs = corpora.readWriteOrThrow(corpus, user).documents
        docs.deleteOrThrow(document)
    }

    private fun uploadZipFile(file: MultipartFile, corpus: UUID, user: User) {
        logger.debug("Unzipping ${file.originalFilename}")
        val exceptions = HashMap<String, Exception>()
        val futures =
            ZipInputStream(BufferedInputStream(file.inputStream)).use { stream ->
                generateSequence { stream.nextEntry }
                    .filterNot { it.isDirectory }
                    .map { entry ->
                        val fileName = Paths.get(entry.name).fileName.toString()
                        val entryData = stream.readBytes()
                        ThreadPoolUtil.pool.submit {
                            logger.debug(
                                "Unzipping ${entry.name} in thread ${Thread.currentThread().name}"
                            )
                            try {
                                createDocumentWithSourceLayer(
                                    corpus,
                                    user,
                                    fileName,
                                    entryData.inputStream(),
                                )
                            } catch (e: Exception) {
                                exceptions[fileName] = e
                            }
                        }
                    }
                    .toList()
            }
        // Wait for all futures to complete
        futures.forEach { it.get() }
        // Check if any exceptions were thrown
        if (exceptions.isNotEmpty()) {
            var message = "${exceptions.size} exceptions encountered: "
            exceptions
                .toList()
                .mapIndexed { index, pair ->
                    message += "(${index + 1}) "
                    message += "File ${pair.first}:"
                    message += pair.second
                }
                .joinToString { " --- " }
            throw FileUploadException(message)
        }
    }

    private fun createDocumentWithSourceLayer(
        corpus: UUID,
        user: User,
        fileName: String,
        input: InputStream,
    ) {
        // tmp file for processing
        val tmpDir: File = createTempDirectory().toFile()
        val file = tmpDir.resolve(fileName)
        file.outputStream().use { input.copyTo(it) }
        // access the corpus as writer
        val docs = corpora.readWriteOrThrow(corpus, user).documents
        // create the document
        try {
            docs.createOrThrow(file)
        } catch (e: Exception) {
            // Document is somehow invalid.
            // Show error to user, but don't save the file
            docs.deleteOrNull(file.name)
            throw DocumentInvalidException(file.name, e.message)
        }
    }
}
