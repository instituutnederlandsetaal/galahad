package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.comparison.LayerFilter

/**
 * The benchmark [Metric]s of a corpus for two different tagger layers.
 * A CorpusMetrics is the sum of the [DocumentMetrics]s of all documents in the corpus.
 */
class CorpusMetrics(
    corpus: Corpus,
    settings: List<MetricsSettings>,
    hypothesis: String,
    reference: String = SOURCE_LAYER_NAME,
    layerFilter: LayerFilter? = null,
    truncate: Boolean = true,
) : Metrics(corpus, settings, hypothesis, reference, truncate = truncate) {

    private val hypothesisJob = corpus.jobs.readOrThrow(hypothesis)
    private val referenceJob = corpus.jobs.readOrThrow(reference)

    @JsonProperty
    val hypothesisLastModified = hypothesisJob.lastModified

    @JsonProperty
    val referenceLastModified = referenceJob.lastModified

    @JsonProperty
    val generated = System.currentTimeMillis()

    init {
        corpus.documents.readAll().forEach {
            add(
                DocumentMetrics(
                    corpus,
                    hypothesisJob.layer(it),
                    referenceJob.layer(it),
                    settings,
                    layerFilter,
                    truncate
                )
            )
        }
    }
}