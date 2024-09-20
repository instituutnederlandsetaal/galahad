package org.ivdnt.galahad.data

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.DOCUMENTS_URL
import org.ivdnt.galahad.app.DOCUMENT_RAW_FILE_URL
import org.ivdnt.galahad.app.DOCUMENT_URL
import org.ivdnt.galahad.app.executeAndLogTime
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.document.DocumentMetadata
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.exceptions.DocumentInvalidFormatException
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.exceptions.FileUploadException
import org.ivdnt.galahad.util.setContentDisposition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.xml.sax.SAXParseException
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipFile

class DocumentWriteType(
    val filename: String,
    val inputStream: InputStream,
)

@RestController
class DocumentsController(
    val corpora: CorporaService,
) : Logging {

    @Autowired
    private val request: HttpServletRequest? = null

    @Autowired
    private val response: HttpServletResponse? = null

    fun UUID.readDocs() = corpora.getReadAccessOrThrow(this, request).documents
    fun UUID.writeDocs() = corpora.getWriteAccessOrThrow(this, request).documents
    fun UUID.readJobs() = corpora.getReadAccessOrThrow(this, request).jobs
    fun UUID.writeJobs() = corpora.getWriteAccessOrThrow(this, request).jobs

    @Operation(
        summary = "List all documents metadata",
        description = "List all documents metadata in a corpus."
    )
    @CrossOrigin
    @GetMapping(DOCUMENTS_URL)
    fun getAllDocuments(@PathVariable @Parameter(description = "Corpus UUID") corpus: UUID): Set<DocumentMetadata> {
        return corpus.readDocs().readAll().mapNotNull {
            // Potentially, the uploaded file might no longer exist, so try.
            try {
                it.metadata.expensiveGet()
            } catch (e: Exception) {
                // Consider the document a lost cause.
                deleteDocument(corpus, it.name)
                null
            }
        }.toSet()
    }

    @Operation(
        summary = "Get single document metadata",
        description = "Get the metadata of a single document.",
        responses = [
            ApiResponse(
                responseCode = "404",
                description = "Corpus or document not found.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            ),
            ApiResponse(
                responseCode = "200",
                description = "DocumentMetadata of the requested document."
            )
        ]
    )
    @CrossOrigin
    @GetMapping(DOCUMENT_URL)
    fun getDocument(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
    ): DocumentMetadata? =
        corpus.readDocs().readOrThrow(document).metadata?.expensiveGet()

    @Operation(
        summary = "Upload document or zip file",
        description = "Upload a document or zip file (.zip) with documents.",
        responses = [
            ApiResponse(
                responseCode = "404",
                description = "Corpus not found.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "The uploaded file is invalid.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "The user is not authorized to upload documents. Uploading documents requires write-access.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
            ),
            ApiResponse(
                responseCode = "201",
                description = "Document/zip uploaded.",
            )
        ]
    )
    @CrossOrigin
    @PostMapping(value = [DOCUMENTS_URL], consumes = ["multipart/form-data"])
    fun uploadFile(
        @RequestBody @SwaggerRequestBody(description = "Document or zip file to upload. See the GaLAHaD Help for the supported formats.") file: MultipartFile,
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
    ) {
        logger.info("Upload file ${file.originalFilename} to corpus $corpus")
        response?.status = HttpServletResponse.SC_CREATED
        executeAndLogTime("handelFileUpload") {
            if (file.contentType == "application/zip" || file.contentType == "application/x-zip-compressed" || file.contentType == "application/octet-stream") {
                uploadZipFile(file, corpus)
            } else {
                logger.info("${file.originalFilename} is a single file. Will convert it to document.")
                createDocumentWithSourceLayer(
                    corpus,
                    DocumentWriteType(file.originalFilename.toString(), file.inputStream)
                )
            }
        }
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

    fun createDocumentWithSourceLayer(corpus: UUID, value: DocumentWriteType): String {
        // create the document
        val documentName: String
        try {
            documentName = corpus.writeDocs().create(value)
        } catch (e: SAXParseException) {
            throw DocumentInvalidFormatException(value.filename, e.message)
        }
        catch (e: Exception) {
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
        sourceLayerJob.documentOrThrow(documentName).setResult(document.sourceLayer.read<Layer>())

        return documentName
    }

    private fun invalidateJobCaches(corpus: UUID) {
        val jobs = corpus.writeJobs()
        jobs.readAll().map { it.stateFile.delete() }
    }

    @Operation(
        summary = "Get raw uploaded document file",
        description = "Get the raw file of a document, as originally uploaded by the user.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "The raw file of the document. JSON media type is for errors only.",
                // Although this API response does not produce JSON.
                // For swagger to work, the json media type needs to be defined on the 200 response.
                content = [Content(mediaType = "text/plain, application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Corpus or document not found.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)), mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "403",
                description = "The user is not authorized to download the raw file. Downloading the original files requires write-access.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)), mediaType = "application/json")]
            ),
        ]
    )
    @CrossOrigin
    @GetMapping(DOCUMENT_RAW_FILE_URL, produces = ["text/plain", "application/json"])
    fun getRawFile(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
    ): ByteArray {
        logger.info("Get raw file for $document from corpus $corpus")
        @Suppress("UNREACHABLE_CODE") // This is in fact very much reachable
        return executeAndLogTime("getRawFileForDocument") {
            // consider it to be writing, so that you need writing permissions to download.
            val raw = corpus.writeDocs().readOrThrow(document).getUploadedRawFile()

            response?.contentType = "text/plain" // Default for text files. Even if it really means "unknown text file"
            response?.setContentDisposition(raw.name)
            return raw.readBytes()
        }
    }

    @Operation(
        summary = "Delete single document",
        description = "Delete a document and its jobs.",
        responses = [
            ApiResponse(
                responseCode = "404",
                description = "Corpus or document not found.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            ),
            ApiResponse(
                responseCode = "204",
                description = "Document deleted.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "The user is not authorized to delete documents. Deleting documents requires write-access.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            ),
        ]
    )
    @CrossOrigin
    @DeleteMapping(DOCUMENT_URL)
    fun deleteDocument(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
    ): ResponseEntity<String> {
        // Delete all jobs and results of this document.
        corpus.writeJobs().readAll().forEach { it.documentOrThrow(document).delete() }
        // Invalidate corpus cache
        corpora.readOrNull(corpus)?.invalidateCache()
        // Now delete it
        corpus.writeDocs().delete(document)

        return ResponseEntity.noContent().build()
    }
}
