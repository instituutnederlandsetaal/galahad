package org.ivdnt.galahad.annotations

import org.ivdnt.galahad.evaluation.comparison.nextOrNull

class LayerAligner(
    val hypothesis: Layer,
    val reference: Layer,
): AnnotationReader() {
    override fun read(): Layer {
        val hypoIter = hypothesis.terms.iterator()

        reference.documents.forEach { doc ->
            doc.paragraphs.forEach { paragraph ->
                paragraph.sentences.forEach { sentence ->
                    sentence.terms.forEach { refTerm ->
                        // TODO: implement alignment logic
                        hypoIter.nextOrNull()?.also { hypoTerm ->
                            if (hypoTerm.token == refTerm.token) {
                                terms += hypoTerm.alignedTo(refTerm)
                            }
                        }
                    }
                }
            }
        }
        return Layer(documents.toTypedArray())
    }
}