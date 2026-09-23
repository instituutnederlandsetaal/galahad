package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.exceptions.UserUnauthorizedException
import org.ivdnt.galahad.jobs.JobScheduler
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.web.service.TaggersService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class TaggersController(private val taggersService: TaggersService) : Logging {
    @Autowired private val request: HttpServletRequest? = null

    @Operation(summary = "List all taggers", description = "List the metadata of all taggers.")
    @GetMapping(Endpoints.Taggers.BASE)
    fun getTaggers(): Iterable<Tagger> = Tagger.taggers.values

    @Operation(summary = "Get single tagger", description = "Metadata of the tagger.")
    @ApiResponse(responseCode = "200", description = "Metadata of the tagger.")
    @ApiResponse(
        responseCode = "404",
        description = "The tagger was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Taggers.TAGGER)
    fun getTagger(@PathVariable @Parameter(description = "Tagger name") tagger: String): Tagger =
        Tagger.readOrThrow(tagger)

    @Operation(summary = "Get tagger health", description = "Get the health of a tagger service.")
    @ApiResponse(responseCode = "200", description = "Health of the requested tagger.")
    @ApiResponse(
        responseCode = "404",
        description = "The tagger was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Taggers.HEALTH)
    fun getTaggerHealth(
        @PathVariable @Parameter(description = "Tagger name") tagger: String
    ): Boolean = taggersService.taggerHealth(tagger)

    @Operation(
        summary = "Number of active tagger jobs",
        description = "Get the number of active jobs. Indicates server load.",
    )
    @GetMapping(Endpoints.Taggers.QUEUE)
    fun activeJobs(): Int = JobScheduler.queueSize

    @Operation(
        summary = "Run tagger on all datasets",
        description = "Run a tagger on all available datasets.",
    )
    @ApiResponse(responseCode = "202", description = "Tagger jobs are queued.")
    @ApiResponse(
        responseCode = "404",
        description = "The tagger was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "403",
        description = "User needs write-access to datasets.",
    )
    @PostMapping(Endpoints.Taggers.DATASETS)
    fun runAllDatasets(
        @PathVariable @Parameter(description = "Tagger name") tagger: String
    ): ResponseEntity<String> {
        // Are we admin?
        if (!User.fromRequest(request).admin)
            throw UserUnauthorizedException("Only admins can run taggers on all datasets.")
        // Run all
        taggersService.runAllDatasets(tagger)
        return ResponseEntity.accepted().build()
    }
}
