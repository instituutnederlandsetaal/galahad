package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.*
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.layers.CorpusLayerMetadata
import org.ivdnt.galahad.web.service.JobsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class JobsController(private val jobsService: JobsService) {
    @Autowired private val request: HttpServletRequest? = null

    @Autowired private val response: HttpServletResponse? = null

    private val user
        get() = User.fromRequest(request)

    @Operation(
        summary = "Get all job metadata",
        description = "Get a summary of the state of all tagger jobs.",
    )
    @CrossOrigin
    @GetMapping(Endpoints.Jobs.BASE)
    fun getJobs(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID
    ): List<CorpusLayerMetadata> = jobsService.readAll(corpus, user)

    @Operation(
        summary = "Get single job metadata",
        description = "Get a summary of the state of a tagger job.",
    )
    @ApiResponse(responseCode = "200", description = "The job state.")
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @GetMapping(Endpoints.Jobs.JOB)
    fun getJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ): CorpusLayerMetadata = jobsService.readOrThrow(corpus, job, user)

    @Operation(
        summary = "Start job",
        description = "Start a job. Requires write access to the corpus.",
    )
    @ApiResponse(responseCode = "202", description = "The job progress.")
    @ApiResponse(
        responseCode = "403",
        description =
            "The user is not authorized to start jobs. Starting jobs requires write access to the corpus.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @PostMapping(Endpoints.Jobs.JOB)
    fun postJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ) {
        response?.status = HttpServletResponse.SC_ACCEPTED
        jobsService.createOrThrow(corpus, job, user)
    }

    @Operation(
        summary = "Cancel or delete job",
        description = "Cancel or delete a job. Requires write access to the corpus.",
    )
    @ApiResponse(responseCode = "204", description = "Job cancelled or deleted.")
    @ApiResponse(
        responseCode = "403",
        description =
            "The user is not authorized to cancel or delete jobs. Cancelling or deleting jobs requires write access to the corpus.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @DeleteMapping(Endpoints.Jobs.JOB)
    fun deleteJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ): ResponseEntity<String> {
        jobsService.deleteOrThrow(corpus, job, user)
        return ResponseEntity.noContent().build()
    }
}
