package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.CorpusMetadata
import org.ivdnt.galahad.corpora.MutableCorpusMetadata
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.web.service.CorporaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@RestController
class CorporaController(
    @Autowired private val corporaService: CorporaService,
) : Logging {
    @Autowired
    private val response: HttpServletResponse? = null

    @Autowired
    private val request: HttpServletRequest? = null

    private val user get() = User.fromRequest(request)

    @Operation(
        summary = "List all corpora metadata",
        description = "List the metadata of all corpora the current user has access to, either as owner or shared by others."
    )
    @CrossOrigin
    @GetMapping(Endpoints.Corpora.BASE)
    fun getCorpora(): List<CorpusMetadata> = corporaService.readAll(user)

    @Operation(
        summary = "Create a new corpus",
        description = "Create a new corpus with the provided CorpusMetadata. The user doing the request becomes owner.",
    )
    @ApiResponse(
        responseCode = "201",
        description = "UUID of the created corpus.",
    )
    @ApiResponse(
        responseCode = "400",
        description = "The corpus name is invalid.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "403",
        description = "The user is not authorized to create a corpus as a dataset.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @PostMapping(Endpoints.Corpora.BASE, consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun postCorpus(@RequestBody @SwaggerRequestBody(description = "Corpus metadata.") value: MutableCorpusMetadata): UUID {
        response?.status = HttpServletResponse.SC_CREATED
        return corporaService.createOrThrow(value, user).uuid
    }

    @Operation(
        summary = "Get single corpus metadata", description = "Get the metadata of a corpus."
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "200",
        description = "CorpusMetadata of the requested corpus.",
    )
    @CrossOrigin
    @GetMapping(Endpoints.Corpora.CORPUS)
    fun getCorpus(@PathVariable @Parameter(description = "Corpus UUID") corpus: UUID): CorpusMetadata =
        corporaService.readAsReaderOrThrow(corpus, user).immutableMetadata

    @Operation(
        summary = "Update corpus metadata",
        description = "Update the metadata of an existing corpus.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "The updated metadata.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = CorpusMetadata::class)))],
    )
    @ApiResponse(
        responseCode = "400",
        description = "The corpus name is invalid.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "403",
        description = "The user is not authorized to perform this action. E.g. changing the dataset status as a non-admin or changing metadata as a viewer.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @PatchMapping(Endpoints.Corpora.CORPUS)
    fun patchCorpus(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestBody @SwaggerRequestBody(description = "Corpus metadata.") value: MutableCorpusMetadata,
    ): CorpusMetadata? = corporaService.update(corpus, value, user)

    @Operation(
        summary = "Delete single corpus",
        description = "Delete a corpus, its documents and jobs.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "204", description = "Corpus deleted."
    )
    @ApiResponse(
        responseCode = "403",
        description = "The user is not authorized to delete this corpus. Only the owner is.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @DeleteMapping(Endpoints.Corpora.CORPUS)
    fun deleteCorpus(@PathVariable @Parameter(description = "Corpus UUID") corpus: UUID): ResponseEntity<String> {
        corporaService.delete(corpus, user)
        return ResponseEntity.noContent().build()
    }
}

