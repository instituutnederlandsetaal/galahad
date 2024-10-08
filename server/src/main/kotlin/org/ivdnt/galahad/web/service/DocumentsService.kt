package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.document.DocumentMetadata
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.exceptions.FileUploadException
import org.ivdnt.galahad.data.document.DocumentWriteType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.xml.sax.SAXParseException
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipFile


val ZIP_TYPES = listOf("application/zip", "application/x-zip-compressed", "application/octet-stream")

@Service
class DocumentsService(val corpora: CorporaService) : Logging {

    @Autowired
    private val request: HttpServletRequest? = null

    fun UUID.writeJobs() = corpora.getWriteAccessOrThrow(this, request).jobs
    fun UUID.readDocs() = corpora.getReadAccessOrThrow(this, request).documents
    fun UUID.writeDocs() = corpora.getWriteAccessOrThrow(this, request).documents
    fun UUID.readJobs() = corpora.getReadAccessOrThrow(this, request).jobs

    fun read(corpus: UUID, document: String): Document {
        return corpus.readDocs().readOrThrow(document)
    }

    fun readAll(corpus: UUID): Set<DocumentMetadata> {
        return corpus.readDocs().readAll().mapNotNull {
            // Potentially, the uploaded file might no longer exist, so try.
            try {
                it.metadata.expensiveGet()
            } catch (e: Exception) {
                // Consider the document a lost cause.
                delete(corpus, it.name)
                null
            }
        }.toSet()
    }

    fun create(file: MultipartFile, corpus: UUID) {
        if (file.contentType in ZIP_TYPES) {
            uploadZipFile(file, corpus)
        } else {
            createDocumentWithSourceLayer(
                corpus, DocumentWriteType(file.originalFilename.toString(), file.inputStream)
            )
        }
    }

    fun delete(corpus: UUID, document: String) {
        // Delete all jobs and results of this document.
        corpus.writeJobs().readAll().forEach { it.documentOrNull(document)?.delete() } // Doesn't matter if null.
        // Invalidate corpus cache
        corpora.readOrNull(corpus)?.invalidateCache()
        // Now delete it
        corpus.writeDocs().delete(document)
    }

    private fun uploadZipFile(file: MultipartFile, corpus: UUID) {
        logger.info("${file.originalFilename} is a zip file. Will unzip it.")
        val localFile = File.createTempFile("zip", file.originalFilename!!)
        file.transferTo(localFile)
        val exceptions = HashMap<String, Exception>() // <filename, exception>
        ZipFile(localFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    try {
                        if (!entry.isDirectory && entry.name.split(".").last() != "zip") {
                            logger.info("Unzipped ${entry.name} from ${file.originalFilename}. Will convert it to document.")
                            // The entry might be in a subfolder, so extract the true file name.
                            val fileName = Paths.get(entry.name).fileName.toString()
                            createDocumentWithSourceLayer(corpus, DocumentWriteType(fileName, input))
                        }
                    } catch (e: Exception) {
                        // Some things might go wrong when processing a file, for example the file can be invalid
                        // This is however not a reason not to process the other files
                        // But is an exception to throw, we just collect the exceptions and throw them as one
                        exceptions[entry.name] = e
                    }
                }
            }
        }
        if (exceptions.isNotEmpty()) {
            var message = "${exceptions.size} exceptions encountered: "
            exceptions.toList().mapIndexed { index, pair ->
                message += "(${index + 1}) "
                message += "File ${pair.first}:"
                message += pair.second
            }.joinToString { " --- " }
            throw FileUploadException(message)
        }
    }

    private fun createDocumentWithSourceLayer(corpus: UUID, value: DocumentWriteType): String {
        // create the document
        val documentName: String
        try {
            documentName = corpus.writeDocs().create(value)
        } catch (e: SAXParseException) {
            throw DocumentInvalidException(value.filename, e.message)
        } catch (e: Exception) {
            // Document is somehow invalid.
            // Show error to user, but don't save the file
            corpus.writeDocs().delete(value.filename)
            throw e
        }
        val document: Document = corpus.readDocs().readOrThrow(documentName)
        // Invalidate job caches.
        invalidateJobCaches(corpus)
        // Invalidate corpus cache
        corpora.readOrNull(corpus)?.invalidateCache()
        // Set the sourceLayer as job.
        val sourceLayerJob = corpus.writeJobs().createOrThrow(SOURCE_LAYER_NAME)
        sourceLayerJob.documentOrEmpty(documentName).setResult(document.sourceLayer.read<Layer>())

        return documentName
    }

    private fun invalidateJobCaches(corpus: UUID) {
        val jobs = corpus.writeJobs()
        jobs.readAll().map { it.stateFile.delete() }
    }
}