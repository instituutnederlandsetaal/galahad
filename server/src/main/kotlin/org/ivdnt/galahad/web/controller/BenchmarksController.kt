package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.FileBackedCache
import org.ivdnt.galahad.app.BENCHMARKS_URL
import org.ivdnt.galahad.app.BENCHMARK_URL
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.evaluation.metrics.FlatMetricType
import org.ivdnt.galahad.evaluation.metrics.FlatMetricTypeAssay
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.web.service.CorporaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * {
 *     "dataset-1": {
 *         "posByPos": {
 *             "tagger-1": {
 *                 "micro": { ... }, "macro": { ... }
 *             },
 *             "tagger-2": { ... },
 *         },
 *         "lemmaByLemma": { ... },
 *     },
 *     "dataset-2": { ... },
 * }
 */
typealias BenchmarksMatrix = Map<String, Map<String, FlatMetricTypeAssay>>
typealias MutableAssaysMatrix = MutableMap<String, MutableMap<String, MutableMap<String, FlatMetricType>>>

@RestController
class BenchmarksController(
    val corpora: CorporaService,
) : Logging {

    @Autowired
    private val request: HttpServletRequest? = null

    /**
     * A matrix of 'tagger' -> 'dataset' -> 'FlatMetric' -> 'scores per category',
     * for all datasets corpora that have been tagged with at least one tagger, excluding the sourceLayer.
     */
    val benchmarksMatrix = object : FileBackedCache<BenchmarksMatrix>(corpora.assaysFile, HashMap()) {
        override fun isValid(lastModified: Long): Boolean {
            return corpora.datasets.firstOrNull { it.lastModified > lastModified } == null
            TODO("Maybe just check the validity of the other assays?")
        }

        override fun set(): BenchmarksMatrix {
            // tagger -> dataset -> assay
            val assaysMatrix: MutableAssaysMatrix = HashMap()
            // For all datasets
            corpora.datasets.forEach { dataset ->
                // For all jobs in the dataset
                dataset.jobs.readAll()
                    // Skip the source layer
                    .filter { it.name != SOURCE_LAYER_NAME }
                    // Add the assay to the matrix
                    .forEach { job ->
                        val meta = dataset.metadata.expensiveGet()
                        // Initialize the dataset row if needed
                        if (assaysMatrix[meta.name] == null) {
                            assaysMatrix[meta.name] = HashMap()
                        }
                        val assay = getAssay(meta.uuid, job.name)
                        assay?.forEach {
                            assaysMatrix[meta.name]?.putIfAbsent(it.key, HashMap())
                            assaysMatrix[meta.name]?.get(it.key)?.put(job.name, it.value)
                        }
                    }
            }
            return assaysMatrix
        }
    }

    /**
     * Get the assay for a single job in a specific corpus. Also used to construct [benchmarksMatrix].
     */
    @Operation(
        summary = "Get single benchmark result",
        description = "Get benchmark results for a single job on a single corpus.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Benchmark result.",
    )
    @ApiResponse(
        responseCode = "400", description = "Corpus not a dataset."
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping(BENCHMARK_URL)
    fun getAssay(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ): FlatMetricTypeAssay? {
        return corpora.getReadAccessOrThrow(corpus, request).jobs.readOrNull(job)?.assay?.get<FlatMetricTypeAssay>()
    }

    @Operation(
        summary = "Get all benchmark results",
        description = "Get benchmark results for all corpora and jobs.",
    )
    @CrossOrigin
    @GetMapping(BENCHMARKS_URL)
    fun getAssays(): BenchmarksMatrix {
        return benchmarksMatrix.get<BenchmarksMatrix>()
    }
}