package org.ivdnt.galahad.web.service

import java.io.BufferedInputStream
import java.io.InputStream
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.io.path.createTempDirectory
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.documents.Document
import org.ivdnt.galahad.documents.DocumentMetadata
import org.ivdnt.galahad.exceptions.FileUploadException
import org.ivdnt.galahad.util.ThreadPoolUtil
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

val ZIP_TYPES: List<String> =
    listOf("application/zip", "application/x-zip-compressed", "application/octet-stream")

@Service
class DocumentsService(private val corpora: CorporaService) : Logging {

    fun readAll(corpus: UUID, user: User): List<DocumentMetadata> =
        corpora.readOrThrow(corpus, user).documents.readAll().map { it.metadata }

    fun readOrThrow(corpus: UUID, document: String, user: User): Document =
        corpora.readOrThrow(corpus, user).documents.readOrThrow(document)

    fun createOrThrow(corpus: UUID, file: MultipartFile, user: User) {
        if (file.contentType in ZIP_TYPES) {
            uploadZipFile(corpus, file, user)
        } else {
            createOrThrow(corpus, file.originalFilename!!, file.inputStream, user)
        }
    }

    fun deleteOrThrow(corpus: UUID, document: String, user: User) {
        // Delete all jobs and results of this document.
        corpora.writeOrThrow(corpus, user).jobs.readAll().forEach {
            it.results.deleteOrNull(document) // Doesn't matter if null.
        } // TODO: delete all evaluations
        // Now delete it as write access
        corpora.writeOrThrow(corpus, user).documents.deleteOrThrow(document)
    }

    private fun uploadZipFile(corpus: UUID, file: MultipartFile, user: User) {
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
                                createOrThrow(corpus, fileName, entryData.inputStream(), user)
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

    private fun createOrThrow(corpus: UUID, fileName: String, input: InputStream, user: User) {
        val file = createTempDirectory().toFile().resolve(fileName)
        input.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        corpora.writeOrThrow(corpus, user).documents.createOrThrow(file)
    }
}
