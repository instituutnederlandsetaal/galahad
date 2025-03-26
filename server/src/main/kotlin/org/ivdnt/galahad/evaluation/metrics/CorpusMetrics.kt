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



    @JsonProperty
    val hypothesisLastModified: Long = hypothesisJob.lastModified

    @JsonProperty
    val referenceLastModified: Long = referenceJob.lastModified

    @JsonProperty
    val generated: Long = System.currentTimeMillis()

    init {
        corpus.documents.readAll().forEach { doc ->
            add(
                DocumentMetrics(
                    corpus,
                    doc,
                    hypothesis,
                    reference,
                    settings,
                    layerFilter,
                    truncate
                )
            )
        }
    }
}