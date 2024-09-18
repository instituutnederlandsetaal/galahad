package org.ivdnt.galahad.data

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.*
import org.ivdnt.galahad.data.corpus.CorpusMetadata
import org.ivdnt.galahad.data.corpus.MutableCorpusMetadata
import org.ivdnt.galahad.exceptions.CorpusNameInvalidException
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.CorpusUnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@RestController
class CorporaController(
    private val corporaService: CorporaService,
) : Logging {

    @Operation(
        summary = "List all corpora",
        description = "List all corpora the current user has access to, either as owner or shared by others."
    )
    @CrossOrigin
    @GetMapping(CORPORA_URL)
    fun getUserCorpora(): Set<CorpusMetadata> = corporaService.readAll().map { it.metadata.expensiveGet() }.toSet()

    @Operation(
        summary = "List benchmark datasets",
        description = "List all benchmark datasets, available to anyone for viewing and evaluation."
    )
    @CrossOrigin
    @GetMapping(DATASETS_CORPORA_URL)
    fun getDatasetsCorpora(): Set<CorpusMetadata> = corporaService.datasets.map { it.metadata.expensiveGet() }.toSet()


    @Operation(
        summary = "Get single corpus",
        description = "Get the metadata of a corpus.",
        responses = [
            ApiResponse(
                responseCode = "404",
                description = "The corpus was not found.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            ),
            ApiResponse(
                responseCode = "200",
                description = "CorpusMetadata of the requested corpus.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = CorpusMetadata::class)))]
            ),
        ]
    )
    @CrossOrigin
    @GetMapping(CORPUS_URL)
    fun getCorpus(@PathVariable @Parameter(description = "Corpus UUID") corpus: UUID): CorpusMetadata {
        return handleExceptions { corporaService.readOrThrow(corpus) }.metadata.expensiveGet()
    }

    @Operation(
        summary = "Create a new corpus",
        description = "Create a new corpus with the provided CorpusMetadata. The user doing the request becomes owner.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "UUID of the created corpus.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = UUID::class)))]
            ), ApiResponse(
                responseCode = "400",
                description = "The corpus name is invalid.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            ), ApiResponse(
                responseCode = "403",
                description = "The user is not authorized to create a corpus as a dataset.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            )
        ]
    )
    @CrossOrigin
    @PostMapping(value = [CORPORA_URL], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun postCorpus(@RequestBody @SwaggerRequestBody(description = "Corpus metadata.") value: MutableCorpusMetadata): UUID {
        return handleExceptions { corporaService.create(value) }
    }

    @Operation(
        summary = "Update corpus metadata",
        description = "Update the metadata of an existing corpus.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "The updated metadata.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = CorpusMetadata::class)))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "The corpus name is invalid.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "The user is not authorized to perform this action. E.g. changing the dataset status as a non-admin or changing metadata as a viewer.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "The corpus was not found.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            )
        ]
    )
    @CrossOrigin
    @PatchMapping(CORPUS_URL)
    fun patchCorpus(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestBody @SwaggerRequestBody(description = "Corpus metadata.") value: MutableCorpusMetadata,
    ): CorpusMetadata? {
        return handleExceptions { corporaService.update(corpus, value) }
    }

    @Operation(
        summary = "Delete single corpus",
        description = "Delete a corpus, its documents and jobs.",
        responses = [
            ApiResponse(
                responseCode = "404",
                description = "The corpus was not found.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            ),
            ApiResponse(
                responseCode = "204",
                description = "Corpus deleted."
            ),
            ApiResponse(
                responseCode = "403",
                description = "The user is not authorized to delete this corpus. Only the owner is.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
            )
        ]
    )
    @CrossOrigin
    @DeleteMapping(CORPUS_URL)
    fun deleteCorpus(@PathVariable @Parameter(description = "Corpus UUID") corpus: UUID): ResponseEntity<String> {
        handleExceptions { corporaService.delete(corpus) }
        return ResponseEntity.noContent().build()
    }

    companion object {
        fun <T> handleExceptions(func: () -> T): T {
            try {
                return func()
            } catch (e: CorpusNotFoundException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
            } catch (e: CorpusUnauthorizedException) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message)
            } catch (e: CorpusNameInvalidException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
            }
        }
    }
}

