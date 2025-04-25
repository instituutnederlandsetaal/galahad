package org.ivdnt.galahad.evaluation.metrics

import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.documents.Document
import org.ivdnt.galahad.jobs.Job
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.LayerFilter
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.taggers.Tagger

/**
 * The benchmark [Metric]s of a document for two different tagger layers.
 */
class DocumentMetrics(
    doc: Document,
    hypoTagger: Tagger,
    refTagger: Tagger,
    hypothesisJob: Job,
    referenceJob: Job,
    settings: List<MetricsSettings>,
    layerFilter: LayerFilter? = null,
    truncate: Boolean = true,
) : Metrics(settings, hypoTagger, refTagger, hypothesisJob, referenceJob, truncate = truncate) {

    init {
        val layerComparison = LayerComparison(hypothesisJob.getLayer(doc), referenceJob.getLayer(doc), layerFilter)

        layerComparison.matches.forEach(this::add)

        layerComparison.referenceTermsWithoutMatches.forEach {
            add(
                TermComparison(hypoTerm = Term.EMPTY, refTerm = it)
            )
        }
    }
}