package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.CORPORA_URL
import org.ivdnt.galahad.app.CORPUS_URL
import org.ivdnt.galahad.app.DATASETS_CORPORA_URL
import org.ivdnt.galahad.data.corpus.CorpusMetadata
import org.ivdnt.galahad.data.corpus.MutableCorpusMetadata
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
    private val corporaService: CorporaService,
) : Logging {
    @Autowired
    private val response: HttpServletResponse? = null

    @Operation(
        summary = "List all corpora metadata",
        description = "List the metadata of all corpora the current user has access to, either as owner or shared by others."
    )
    @CrossOrigin
    @GetMapping(CORPORA_URL)
    fun getUserCorpora(): Set<CorpusMetadata> = corporaService.readAll().map { it.metadata.expensiveGet() }.toSet()

    @Operation(
        summary = "List benchmark datasets",
        description = "List the metadata of all benchmark datasets, available to anyone for viewing and evaluation."
    )
    @CrossOrigin
    @GetMapping(DATASETS_CORPORA_URL)
    fun getDatasetsCorpora(): Set<CorpusMetadata> = corporaService.datasets.map { it.metadata.expensiveGet() }.toSet()


    @Operation(
        summary = "Get single corpus metadata",
        description = "Get the metadata of a corpus."
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
    @GetMapping(CORPUS_URL)
    fun getCorpus(@PathVariable @Parameter(description = "Corpus UUID") corpus: UUID): CorpusMetadata {
        return corporaService.readOrThrow(corpus).metadata.expensiveGet()
    }

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
    @PostMapping(value = [CORPORA_URL], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun postCorpus(@RequestBody @SwaggerRequestBody(description = "Corpus metadata.") value: MutableCorpusMetadata): UUID {
        response?.status = HttpServletResponse.SC_CREATED
        return corporaService.createOrThrow(value).metadata.expensiveGet().uuid
    }

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
    @PatchMapping(CORPUS_URL)
    fun updateCorpus(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestBody @SwaggerRequestBody(description = "Corpus metadata.") value: MutableCorpusMetadata,
    ): CorpusMetadata? {
        return corporaService.update(corpus, value)
    }

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
        responseCode = "204",
        description = "Corpus deleted."
    )
    @ApiResponse(
        responseCode = "403",
        description = "The user is not authorized to delete this corpus. Only the owner is.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @DeleteMapping(CORPUS_URL)
    fun deleteCorpus(@PathVariable @Parameter(description = "Corpus UUID") corpus: UUID): ResponseEntity<String> {
        corporaService.delete(corpus)
        return ResponseEntity.noContent().build()
    }
}

