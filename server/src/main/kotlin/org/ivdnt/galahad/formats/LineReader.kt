package org.ivdnt.galahad.formats

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.TermSpan

abstract class LineReader : AnnotationReader() {
    /**
     * Override newSentence to insert ner spans
     */
    override fun newSentence() {
        buildList<Pair<String, MutableList<Int>>> {
            terms.forEachIndexed { i, t ->
                if (t.ner?.startsWith("B-") == true) {
                    add(t.annotationHead(org.ivdnt.galahad.annotations.Annotation.NER)!! to mutableListOf(i))
                } else if (t.ner?.startsWith("I-") == true) {
                    last().second.add(i)
                }
            }
        }.ifEmpty { null }?.map { (value, indices) -> TermSpan(indices, value) }
            ?.let { spans[Annotation.NER] = it.toMutableList() }
        super.newSentence()
    }
}