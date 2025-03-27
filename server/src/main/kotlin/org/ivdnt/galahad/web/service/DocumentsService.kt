package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.documents.Document
import org.ivdnt.galahad.corpora.documents.DocumentMetadata
import org.ivdnt.galahad.corpora.documents.Documents
import org.ivdnt.galahad.corpora.jobs.Jobs
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.exceptions.FileUploadException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipFile
import kotlin.io.path.createTempDirectory


val ZIP_TYPES: List<String> = listOf("application/zip", "application/x-zip-compressed", "application/octet-stream")

@Service
class DocumentsService(val corpora: CorporaService) : Logging {

    @Autowired
    private val request: HttpServletRequest? = null

    private val user get() = User.fromRequest(request)

    fun UUID.writeJobs(): Jobs = corpora.readAsWriterOrThrow(this, user).jobs
    fun UUID.readDocs(): Documents = corpora.readAsReaderOrThrow(this, user).documents
    fun UUID.writeDocs(): Documents = corpora.readAsWriterOrThrow(this, user).documents
    fun UUID.readJobs(): Jobs = corpora.readAsReaderOrThrow(this, user).jobs


    fun read(corpus: UUID, document: String): Document = corpus.readDocs().readOrThrow(document)

    fun readAll(corpus: UUID): Set<DocumentMetadata> = corpus.readDocs().readAll().map { it.metadata }.toSet()

    // TODO: check if we still need to delete lost causes

    // return corpus.readDocs().readAll().mapNotNull {
    //     // Potentially, the uploaded file might no longer exist, so try.
    //     try {
    //         it.metadata
    //     } catch (e: Exception) {
    //         // Consider the document a lost cause.
    //         delete(corpus, it.name)
    //         null
    //     }
    // }.toSet()

    fun create(file: MultipartFile, corpus: UUID) {
        if (file.contentType in ZIP_TYPES) {
            uploadZipFile(file, corpus)
        } else {
            createDocumentWithSourceLayer(
                corpus, file.originalFilename, file.inputStream
            )
        }
    }

    fun delete(corpus: UUID, document: String) {
        // Delete all jobs and results of this document.
        corpus.writeJobs().readAll().forEach { it.jobDocuments.deleteOrNull(document) } // Doesn't matter if null.
        // Now delete it
        corpus.writeDocs().deleteOrThrow(document)
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
                            createDocumentWithSourceLayer(corpus, fileName, input)
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

    private fun createDocumentWithSourceLayer(corpus: UUID, fileName: String, input: InputStream) {
        // tmp file for processing
        val tmpDir: File = createTempDirectory("upload").toFile()
        val file = tmpDir.resolve(fileName)
        file.outputStream().use { input.copyTo(it) }
        // access the corpus
        val docs = corpus.writeDocs()
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