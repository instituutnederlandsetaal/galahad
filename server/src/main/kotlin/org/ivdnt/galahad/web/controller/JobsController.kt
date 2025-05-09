package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.app.JOBS_URL
import org.ivdnt.galahad.app.JOB_URL
import org.ivdnt.galahad.app.TAGGERS_URL
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.jobs.JobController
import org.ivdnt.galahad.jobs.JobMetadata
import org.ivdnt.galahad.jobs.Jobs
import org.ivdnt.galahad.jobs.Progress
import org.ivdnt.galahad.web.service.CorporaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class JobsController(
    val corpora: CorporaService,
) {
    @Autowired
    private val request: HttpServletRequest? = null

    @Autowired
    private val response: HttpServletResponse? = null
    private val user get() = User.fromRequest(request)

    fun UUID.readJobs(): Jobs = corpora.readAsReaderOrThrow(this, user).jobs
    fun UUID.writeJobs(): Jobs = corpora.readAsWriterOrThrow(this, user).jobs

    // TODO could this be replaced by /taggers?
    @Operation(
        summary = "Get all job metadata",
        description = "Get a summary of the state of all tagger jobs.",
    )
    @CrossOrigin
    @GetMapping(JOBS_URL)
    fun getJobs(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestParam(defaultValue = "true") @Parameter(description = "Only show jobs that have a result. Otherwise also shows potential jobs.") hasResult: Boolean = true,
    ): List<JobMetadata> = corpus.readJobs().readAll().map { it.metadata }.toList()

    @Operation(
        summary = "Get single job metadata",
        description = "Get a summary of the state of a tagger job.",
    )
    @ApiResponse(
        responseCode = "200", description = "The job state."
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(JOB_URL)
    fun getJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ): JobMetadata = corpus.readJobs().readOrThrow(job).metadata

    @Operation(
        summary = "Start job",
        description = "Start a job. Requires write access to the corpus.",
    )
    @ApiResponse(
        responseCode = "202", description = "The job progress."
    )
    @ApiResponse(
        responseCode = "403",
        description = "The user is not authorized to start jobs. Starting jobs requires write access to the corpus.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "The sourceLayer is not a tagger.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @PostMapping(JOB_URL)
    fun postJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ): Progress {
        corpus.writeJobs().createOrThrow(job).start()
        response?.status = HttpServletResponse.SC_ACCEPTED
        return progress(corpus, job)
    }

    @Operation(
        summary = "Cancel or delete job",
        description = "Cancel or delete a job. Requires write access to the corpus.",
    )
    @ApiResponse(
        responseCode = "204", description = "Job cancelled or deleted."
    )
    @ApiResponse(
        responseCode = "403",
        description = "The user is not authorized to cancel or delete jobs. Cancelling or deleting jobs requires write access to the corpus.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "The sourceLayer is not a tagger.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @DeleteMapping(JOB_URL)
    fun cancelOrDeleteJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
        @RequestParam @Parameter(description = "Whether to only cancel the current tagging queue (soft), or delete all job results (hard)") hard: Boolean,
    ): ResponseEntity<String> {
        if (hard) {
            corpus.writeJobs().deleteOrThrow(job)
        } else {
            corpus.writeJobs().readOrThrow(job).stop()
        }
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Get job result sample",
        description = "Get a sample summary of the resulting tagged layer of a job.",
    )
    @ApiResponse(
        responseCode = "200", description = "The job layer result."
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus, document or job result was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "The sourceLayer is not a tagger.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping("${JOB_URL}/documents/{document}/result")
    fun getDocumentResult(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
        @PathVariable @Parameter(description = "Document name") document: String,
    ): Layer = corpus.readJobs().readOrThrow(job).results.readOrThrow(document).layer!!

    @Operation(
        summary = "Get job progress",
        description = "Get the progress of a job.",
    )
    @ApiResponse(
        responseCode = "200", description = "The job progress."
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "The sourceLayer is not a tagger.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping("$JOB_URL/progress")
    fun progress(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ): Progress = corpus.readJobs().readOrThrow(job).progress

    @Operation(
        summary = "Number of active tagger jobs",
        description = "Get the number of active jobs. Indicates server load."
    )
    @CrossOrigin
    @GetMapping("$TAGGERS_URL/active")
    fun activeJobs(): Int = JobController.queueSize
}