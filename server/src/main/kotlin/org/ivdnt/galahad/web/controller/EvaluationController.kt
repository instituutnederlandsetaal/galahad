package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.evaluation.confusion.JobConfusion
import org.ivdnt.galahad.evaluation.distribution.JobDistribution
import org.ivdnt.galahad.evaluation.distribution.TypeToken
import org.ivdnt.galahad.evaluation.entities.CorpusEntities
import org.ivdnt.galahad.evaluation.metrics.DocumentMetric
import org.ivdnt.galahad.evaluation.metrics.JobMetric
import org.ivdnt.galahad.evaluation.spans.DocumentSpanEvaluation
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.web.service.EvaluationService
import org.springframework.web.bind.annotation.*

@RestController
class EvaluationController(val evaluationService: EvaluationService) : Logging {

    @Operation(
        summary = "Get distribution",
        description = "Get the distribution of annotations in a corpus for a specific job.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "The distribution of annotations in the corpus.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.DISTRIBUTION)
    fun getJobDistribution(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") hypothesis: String,
    ): JobDistribution = evaluationService.getJobDistribution(corpus, hypothesis)

    @Operation(
        summary = "Get document layer comparison",
        description = "A comparison between two tagger jobs on document level. Sequential tokens.",
    )
    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.Document.COMPARISON)
    fun getLayerComparison(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") hypothesis: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") reference: String,
    ): List<TermComparison> =
        evaluationService.getLayerComparison(corpus, document, hypothesis, reference)

    @Operation(
        summary = "Get confusion",
        description =
            "Get the confusion matrix for a job in a corpus. Returns a map of annotation types to confusion matrices for all supported annotations.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "A map of annotation types to confusion matrices.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.CONFUSION)
    fun getJobConfusion(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") hypothesis: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") reference: String,
    ): JobConfusion = evaluationService.getJobConfusion(corpus, hypothesis, reference)

    @Operation(
        summary = "Get confusion samples",
        description =
            "Samples of tokens that are confused in a corpus for a specific job, filtered by, e.g., a specific part of speech.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "A zip file containing the samples in csv format.",
        content = [Content(mediaType = "application/zip,*/*")],
    )
    @ApiResponse(
        responseCode = "400",
        description =
            "The annotation type does not exist (or misspelled) or is not supported by the tagger.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.CONFUSION_DOWNLOAD)
    fun getConfusionSamples(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") hypothesis: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") reference: String,
        @RequestParam
        @Parameter(description = "Annotation type for which to generate the confusion")
        annotation: Annotation,
        @RequestParam @Parameter(description = "Annotation head to filter on") hypoFilter: String,
        @RequestParam @Parameter(description = "Annotation head to filter on") refFilter: String,
    ): ByteArray =
        evaluationService.getConfusionSamples(
            hypoFilter,
            refFilter,
            annotation,
            corpus,
            hypothesis,
            reference,
        )

    @Operation(
        summary = "Get metrics",
        description =
            "Get detailed accuracy metrics for a job in a corpus compared to a ground truth reference.",
    )
    @ApiResponse(responseCode = "200", description = "A map of annotation types to metrics.")
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.METRICS)
    fun getMetrics(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") hypothesis: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") reference: String,
    ): JobMetric = evaluationService.getJobMetric(corpus, hypothesis, reference)

    @Operation(
        summary = "Get metrics samples",
        description =
            "Samples of tokens in a specific grouping (e.g. the NOU-C group for PoS), or a specific statistical hypothesis class (e.g. true positive).",
    )
    @ApiResponse(
        responseCode = "200",
        description = "A zip file containing the samples in csv format.",
        content = [Content(mediaType = "application/zip,*/*")],
    )
    @ApiResponse(
        responseCode = "400",
        description = "The setting or classification type does not exist.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.METRICS_DOWNLOAD)
    fun getMetricsSamples(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") hypothesis: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") reference: String,
        @RequestParam
        @Parameter(description = "Metrics type (e.g. posByPos, lemmaByLemma)")
        metricsType: String,
        @RequestParam("class")
        @Parameter(description = "Classification type(e.g. true positive)")
        classType: String,
        @RequestParam @Parameter(description = "Annotation head (e.g. NOU-C)") group: String? = null,
    ): ByteArray =
        evaluationService.getMetricsSamples(
            metricsType,
            group,
            corpus,
            hypothesis,
            reference,
            classType,
        )

    @Operation(
        summary = "Download evaluation",
        description =
            "Download a zip containing all combinations of evaluations (metrics, distribution, confusion) and (available) annotations (e.g. pos, depending on the tagger).",
    )
    @ApiResponse(
        responseCode = "200",
        description = "A zip containing all combinations of evaluations.",
        content = [Content(mediaType = "application/zip,*/*")],
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.DOWNLOAD)
    fun download(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") hypothesis: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") reference: String,
    ): ByteArray = evaluationService.getEvaluation(corpus, hypothesis, reference)

    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.ENTITIES)
    fun getJobEntities(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID
    ): CorpusEntities = evaluationService.getCorpusEntities(corpus)

    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.Document.DISTRIBUTION)
    fun getDocumentDistribution(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") hypothesis: String,
    ): Map<Annotation, List<TypeToken>> =
        evaluationService.getDocumentDistribution(corpus, hypothesis, document).typeTokens

    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.Document.METRICS)
    fun getDocumentMetric(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") hypothesis: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") reference: String,
    ): DocumentMetric = evaluationService.getDocumentMetric(corpus, document, hypothesis, reference)

    @CrossOrigin
    @GetMapping(Endpoints.Evaluation.Document.SPANS)
    fun getDocumentSpanEvaluation(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") hypothesis: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") reference: String,
    ): DocumentSpanEvaluation =
        evaluationService.getDocumentSpanEvaluation(corpus, document, hypothesis, reference)
}
