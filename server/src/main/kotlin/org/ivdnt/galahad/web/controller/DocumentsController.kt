package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.DOCUMENTS_URL
import org.ivdnt.galahad.app.DOCUMENT_RAW_FILE_URL
import org.ivdnt.galahad.app.DOCUMENT_URL
import org.ivdnt.galahad.documents.DocumentMetadata
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.util.setContentDisposition
import org.ivdnt.galahad.web.service.CorporaService
import org.ivdnt.galahad.web.service.DocumentsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

// Note that some API responses have */* as content type.
// For swagger to work, all media types have to be defined on the 200 response.
@RestController
class DocumentsController(
    val corpora: CorporaService,
    val documentsService: DocumentsService,
) : Logging {

    @Autowired
    private val response: HttpServletResponse? = null

    @Operation(
        summary = "List all documents metadata",
        description = "List all documents metadata in a corpus.",
    )
    @ApiResponse(
        responseCode = "200", description = "DocumentMetadata of all documents in the corpus."
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @GetMapping(DOCUMENTS_URL)
    fun getAllDocuments(@PathVariable @Parameter(description = "Corpus UUID") corpus: UUID): List<DocumentMetadata> =
        documentsService.readAll(corpus)

    @Operation(
        summary = "Get single document metadata",
        description = "Get the metadata of a single document.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or document not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "200", description = "DocumentMetadata of the requested document."
    )
    @CrossOrigin
    @GetMapping(DOCUMENT_URL)
    fun getDocument(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
    ): DocumentMetadata? = documentsService.read(corpus, document).metadata

    @Operation(
        summary = "Upload document or zip file",
        description = "Upload a document or zip file (.zip) with documents.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "The uploaded file is invalid.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "403",
        description = "The user is not authorized to upload documents. Uploading documents requires write-access.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "201",
        description = "Document/zip uploaded.",
    )
    @CrossOrigin
    @PostMapping(value = [DOCUMENTS_URL], consumes = ["multipart/form-data"])
    fun uploadFile(
        @RequestBody @SwaggerRequestBody(description = "Document or zip file to upload. See the GaLAHaD Help for the supported formats.") file: MultipartFile,
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
    ) {
        response?.status = HttpServletResponse.SC_CREATED
        documentsService.create(file, corpus)
    }

    @Operation(
        summary = "Get raw uploaded document file",
        description = "Get the raw file of a document, as originally uploaded by the user.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "The raw file of the document.",
        content = [Content(mediaType = "text/plain,*/*")],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or document not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "403",
        description = "The user is not authorized to download the raw file. Downloading the original files requires write-access.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(DOCUMENT_RAW_FILE_URL)
    fun getRawFile(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
    ): ByteArray {
        val rawFile = documentsService.read(corpus, document).uploadedFile
        response?.contentType = "text/plain" // Default for text files. Even if it really means "unknown text file"
        response?.setContentDisposition(rawFile.name)
        return rawFile.readBytes()
    }

    @Operation(
        summary = "Delete single document",
        description = "Delete a document and its jobs.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or document not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "204",
        description = "Document deleted.",
    )
    @ApiResponse(
        responseCode = "403",
        description = "The user is not authorized to delete documents. Deleting documents requires write-access.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @DeleteMapping(DOCUMENT_URL)
    fun deleteDocument(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
    ): ResponseEntity<String> {
        documentsService.delete(corpus, document)
        return ResponseEntity.noContent().build()
    }
}
