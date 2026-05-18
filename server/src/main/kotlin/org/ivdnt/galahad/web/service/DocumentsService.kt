package org.ivdnt.galahad.web.service

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.Document
import org.ivdnt.galahad.documents.DocumentMetadata
import org.ivdnt.galahad.exceptions.FileUploadException
import org.ivdnt.galahad.util.ThreadPoolUtil
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.InputStream
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.io.path.createTempDirectory

@Service
class DocumentsService(private val corpora: CorporaService) : Logging {
    fun readAll(corpus: UUID): List<DocumentMetadata> =
        corpora.readOrThrow(corpus).documents.readAll().map { it.metadata }

    fun readOrThrow(corpus: UUID, document: String): Document =
        corpora.readOrThrow(corpus).documents.readOrThrow(document)

    fun createOrThrow(corpus: UUID, file: MultipartFile) {
        val corpus: Corpus = corpora.writeOrThrow(corpus)
        if (file.contentType in ZIP_TYPES) {
            uploadZipFile(corpus, file)
        } else {
            createOrThrow(corpus, file.originalFilename!!, file.inputStream)
        }
    }

    fun deleteOrThrow(corpus: UUID, document: String) {
        // Delete all jobs and results of this document.
        corpora.writeOrThrow(corpus).jobs.readAll().forEach {
            it.results.deleteOrNull(document) // Doesn't matter if null.
        } // TODO: delete all evaluations
        // Now delete it as write access
        corpora.writeOrThrow(corpus).documents.deleteOrThrow(document)
    }

    private fun uploadZipFile(corpus: Corpus, file: MultipartFile) {
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
                                createOrThrow(corpus, fileName, entryData.inputStream())
                            } catch (e: Exception) {
                                exceptions[fileName] = e
                            }
                        }
                    }
                    .toList()
            }
        // TODO use completion service like CorpusMetrics?
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

    private fun createOrThrow(corpus: Corpus, fileName: String, input: InputStream) {
        val file = createTempDirectory().toFile().resolve(fileName)
        input.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        corpus.documents.createOrThrow(file)
    }

    companion object {
        private val ZIP_TYPES: List<String> =
            listOf("application/zip", "application/x-zip-compressed", "application/octet-stream")
    }
}
