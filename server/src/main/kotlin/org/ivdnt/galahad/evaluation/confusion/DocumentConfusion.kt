package org.ivdnt.galahad.evaluation.confusion

import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.LayerFilter
import org.ivdnt.galahad.evaluation.comparison.TermComparison

/**
 * Part of speech confusion of a document for two different tagger layers.
 */
class DocumentConfusion(
    hypothesis: Layer,
    reference: Layer,
    layerFilter: LayerFilter? = null,
    annotation: AnnotationType = AnnotationType.POS,
) : Confusion(truncate = layerFilter == null, annotation) {

    init {
        val layerComparison = LayerComparison(
            hypothesisLayer = hypothesis,
            referenceLayer = reference,
            layerFilter = layerFilter
        )

        layerComparison.matches.forEach(::add)

        layerComparison.hypothesisTermsWithoutMatches.forEach {
            add(
                hypoPos = it.annotationHeadOrMissing(annotation),
                refPos = TermComparison.MISSING_MATCH,
                sample = TermComparison(hypoTerm = it, refTerm = Term.EMPTY)
            )
        }

        layerComparison.referenceTermsWithoutMatches.forEach {
            add(
                hypoPos = TermComparison.MISSING_MATCH,
                refPos = it.annotationHeadOrMissing(annotation),
                sample = TermComparison(hypoTerm = Term.EMPTY, refTerm = it)
            )
        }
    }
}