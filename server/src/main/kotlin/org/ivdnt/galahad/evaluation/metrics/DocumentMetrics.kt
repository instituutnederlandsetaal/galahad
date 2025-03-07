package org.ivdnt.galahad.evaluation.metrics

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.LayerFilter
import org.ivdnt.galahad.evaluation.comparison.TermComparison

/**
 * The benchmark [Metric]s of a document for two different tagger layers.
 */
class DocumentMetrics(
    corpus: Corpus,
    hypothesis: Layer,
    reference: Layer,
    settings: List<MetricsSettings>,
    layerFilter: LayerFilter? = null,
    truncate: Boolean = true,
) : Metrics(corpus, settings, hypothesis.name, reference.name, truncate = truncate) {

    init {
        val layerComparison = LayerComparison(hypothesis, reference, layerFilter)

        layerComparison.matches.forEach(this::add)

        layerComparison.referenceTermsWithoutMatches.forEach {
            add(
                TermComparison(hypoTerm = Term.EMPTY, refTerm = it)
            )
        }
    }
}