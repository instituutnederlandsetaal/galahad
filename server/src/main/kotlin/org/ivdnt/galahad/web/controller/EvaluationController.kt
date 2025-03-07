package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.*
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.annotations.AnnotationType
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.evaluation.confusion.Confusion
import org.ivdnt.galahad.evaluation.distribution.CorpusDistribution
import org.ivdnt.galahad.evaluation.metrics.CorpusMetrics
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.web.service.EvaluationService
import org.springframework.web.bind.annotation.*
import java.util.*

const val DISTRIBUTION_MAX_SIZE = 1000

@RestController
class EvaluationController(
    val evaluationService: EvaluationService,
) : Logging {


    @Operation(
        summary = "Get distribution",
        description = "Get the distribution of annotations in a corpus for a specific job."
    )
    @ApiResponse(
        responseCode = "200",
        description = "The distribution of annotations in the corpus.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(DISTRIBUTION_URL)
    fun getDistribution(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name or sourceLayer") job: String,
    ): Map<AnnotationType, CorpusDistribution> {
        return evaluationService.getDistribution(corpus, job)
    }

    @Operation(
        summary = "Get document layer comparison",
        description = "A comparison between two tagger jobs on document level. Sequential tokens."
    )
    @CrossOrigin
    @GetMapping(DOCUMENT_EVALUATION_URL)
    fun getDocumentLevelLayerVisualisation(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
        @PathVariable @Parameter(description = "Tagger name or sourceLayer") job: String,
        @RequestParam(defaultValue = SOURCE_LAYER_NAME) @Parameter(description = "Tagger name or sourceLayer") reference: String? = SOURCE_LAYER_NAME,
    ): List<TermComparison> {
        return evaluationService.getDocumentLevelLayerVisualisation(corpus, document, job, reference)
    }

    @Operation(
        summary = "Get confusion",
        description = "Get the confusion matrix for a job in a corpus. Returns a map of annotation types to confusion matrices for all supported annotations."
    )
    @ApiResponse(
        responseCode = "200",
        description = "A map of annotation types to confusion matrices.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(CONFUSION_URL)
    fun getConfusion(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name or sourceLayer") job: String,
        @RequestParam(defaultValue = SOURCE_LAYER_NAME) @Parameter(description = "Tagger name or sourceLayer") reference: String? = SOURCE_LAYER_NAME,
    ): Map<AnnotationType, Confusion> {
        return evaluationService.getConfusion(corpus, job, reference)
    }

    @Operation(
        summary = "Get confusion samples",
        description = "Samples of tokens that are confused in a corpus for a specific job, filtered by, e.g., a specific part of speech."
    )
    @ApiResponse(
        responseCode = "200",
        description = "A zip file containing the samples in csv format.",
        content = [Content(mediaType = "application/zip,*/*")]
    )
    @ApiResponse(
        responseCode = "400",
        description = "The annotation type does not exist (or misspelled) or is not supported by the tagger.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(CONFUSION_SAMPLES_URL)
    fun getConfusionSamples(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name or sourceLayer") job: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") reference: String,
        @RequestParam @Parameter(description = "Annotation type for which to generate the confusion") annotation: String,
        @RequestParam @Parameter(description = "Annotation head to filter on") hypoFilter: String,
        @RequestParam @Parameter(description = "Annotation head to filter on") refFilter: String,
    ): ByteArray {
        return evaluationService.getConfusionSamples(hypoFilter, refFilter, annotation, corpus, job, reference)
    }

    @Operation(
        summary = "Get metrics",
        description = "Get detailed accuracy metrics for a job in a corpus compared to a ground truth reference."
    )
    @ApiResponse(
        responseCode = "200",
        description = "A map of annotation types to metrics.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(METRICS_URL)
    fun getMetrics(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name or sourceLayer") job: String,
        @RequestParam(defaultValue = SOURCE_LAYER_NAME) @Parameter(description = "Tagger name or sourceLayer") reference: String? = SOURCE_LAYER_NAME,
    ): CorpusMetrics {
        return evaluationService.getMetrics(corpus, job, reference)
    }

    @Operation(
        summary = "Get metrics samples",
        description = "Samples of tokens in a specific grouping (e.g. the NOU-C group for PoS), or a specific statistical hypothesis class (e.g. true positive)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "A zip file containing the samples in csv format.",
        content = [Content(mediaType = "application/zip,*/*")]
    )
    @ApiResponse(
        responseCode = "400",
        description = "The setting or classification type does not exist.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(METRICS_SAMPLES_URL)
    fun getMetricsSamples(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name or sourceLayer") job: String,
        @RequestParam @Parameter(description = "Tagger name or sourceLayer") reference: String,
        @RequestParam @Parameter(description = "Metrics type (e.g. posByPos, lemmaByLemma)") metricsType: String,
        @RequestParam("class") @Parameter(description = "Classification type(e.g. true positive)") classType: String,
        @RequestParam @Parameter(description = "Annotation head (e.g. NOU-C)") group: String? = null,
    ): ByteArray {
        return evaluationService.getMetricsSamples(metricsType, group, corpus, job, reference, classType)
    }

    @Operation(
        summary = "Download evaluation",
        description = "Download a zip containing all combinations of evaluations (metrics, distribution, confusion) and (available) annotations (e.g. pos, depending on the tagger)."
    )
    @ApiResponse(
        responseCode = "200",
        description = "A zip containing all combinations of evaluations.",
        content = [Content(mediaType = "application/zip,*/*")]
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(EVALUATION_CSV_URL)
    fun download(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name or sourceLayer") job: String,
        @RequestParam(defaultValue = SOURCE_LAYER_NAME) @Parameter(description = "Tagger name or sourceLayer") reference: String? = SOURCE_LAYER_NAME,
    ): ByteArray {
        return evaluationService.getEvaluation(corpus, job, reference)
    }

    @CrossOrigin
    @GetMapping(TOKEN_FREQUENCY_URL)
    fun getTokenFrequency(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name or sourceLayer") job: String,
        @RequestParam(defaultValue = SOURCE_LAYER_NAME) @Parameter(description = "Tagger name or sourceLayer") reference: String? = SOURCE_LAYER_NAME,
    ): CorpusMetrics {
        return evaluationService.getTokenFrequency(corpus, job, reference)
    }
}