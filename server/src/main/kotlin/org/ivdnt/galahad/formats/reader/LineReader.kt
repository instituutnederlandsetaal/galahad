package org.ivdnt.galahad.formats.reader

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.TermSpan

abstract class LineReader : LayerReader() {
    /** Override newSentence to insert ner spans */
    override fun newSentence() {
        buildList<Pair<String, MutableList<Int>>> {
                terms.forEachIndexed { i, t ->
                    if (t.ner?.startsWith("B-") == true) {
                        add(t.annotationHead(Annotation.NER)!! to mutableListOf(i))
                    } else if (t.ner?.startsWith("I-") == true) {
                        last().second.add(i)
                    }
                }
            }
            .ifEmpty { null }
            ?.map { (value, indices) -> TermSpan(indices, value) }
            ?.let { spans[Annotation.NER] = it.toMutableList() }
        super.newSentence()
    }
}
