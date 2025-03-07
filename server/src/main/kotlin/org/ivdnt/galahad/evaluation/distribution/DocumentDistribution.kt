package org.ivdnt.galahad.evaluation.distribution

import org.ivdnt.galahad.annotations.AnnotationType
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.corpora.documents.DocumentMetadata

/**
 * The frequency distribution of terms in a document for a specific tagger layer.
 */
class DocumentDistribution(
    hypothesis: Layer,
    meta: DocumentMetadata,
    annotation: AnnotationType,
) : Distribution(annotation) {
    init {
        totalChars = meta.numChars
        totalAlphabeticChars = meta.numAlphabeticChars
        hypothesis.terms.forEach(::add)
    }
}