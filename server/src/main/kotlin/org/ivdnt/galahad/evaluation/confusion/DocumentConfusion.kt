package org.ivdnt.galahad.evaluation.confusion

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.LayerFilter

/**
 * Part of speech confusion of a document for two different tagger layers.
 */
class DocumentConfusion(
    hypothesis: Layer,
    reference: Layer,
    filter: LayerFilter? = null,
    annotation: Annotation = Annotation.POS,
) : Confusion(truncate = filter == null, annotation) {

    init {
        val layerComparison = LayerComparison(hypothesis, reference, filter)

        layerComparison.matches.forEach(::add)
    }
}