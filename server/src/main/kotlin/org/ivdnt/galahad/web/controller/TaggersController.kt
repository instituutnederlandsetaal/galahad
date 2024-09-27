package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.TAGGERS_URL
import org.ivdnt.galahad.app.TAGGER_HEALTH_URL
import org.ivdnt.galahad.app.TAGGER_URL
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.taggers.TaggerHealth
import org.ivdnt.galahad.web.service.TaggersService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TaggersController(
    val taggersService: TaggersService,
) : Logging {
    @Operation(
        summary = "List all taggers",
        description = "List the metadata of all taggers."
    )
    @CrossOrigin
    @GetMapping(TAGGERS_URL)
    fun getTaggers(): Set<Tagger> = taggersService.readAll()

    @Operation(
        summary = "Get single tagger",
        description = "Get the metadata of a single tagger.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Metadata of the requested tagger.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "The tagger was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(TAGGER_URL)
    fun getTagger(@PathVariable @Parameter(description = "Tagger name") tagger: String): Tagger? =
        taggersService.read(tagger)

    @Operation(
        summary = "Get tagger health",
        description = "Get the health of a tagger service.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Health of the requested tagger.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "The tagger was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(TAGGER_HEALTH_URL)
    fun getTaggerHealth(@PathVariable @Parameter(description = "Tagger name") tagger: String): TaggerHealth =
        taggersService.taggerHealth(tagger)


    @Operation(
        summary = "Number of active document jobs",
        description = "Get the number of documents actively being tagged, cumulative over all taggers. Indicates server load."
    )
    @CrossOrigin
    @GetMapping("$TAGGERS_URL/active")
    fun getActiveDocsAtTaggers(): Int = taggersService.numActiveDocuments()
}