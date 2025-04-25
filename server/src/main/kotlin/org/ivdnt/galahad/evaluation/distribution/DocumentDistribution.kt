package org.ivdnt.galahad.evaluation.distribution

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.documents.DocumentMetadata

/**
 * The frequency distribution of terms in a document for a specific tagger layer.
 */
class DocumentDistribution(
    hypothesis: Layer,
    meta: DocumentMetadata,
    annotation: Annotation,
) : Distribution(annotation) {
    init {
        totalChars = meta.numChars
        totalAlphabeticChars = meta.numAlphabeticChars
        hypothesis.terms.forEach(::add)
    }
}