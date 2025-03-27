package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonIgnore

const val SOURCE_LAYER_NAME: String = "sourceLayer"

/**
 * A Annotation Layer may contain multiple documents. (E.g. connlu "newdoc id", tei "<text>")
 * Those may be split into paragraphs, sentences, etc.
 */
class Layer(
    val documents: List<DocumentLayer>
) {
    @get:JsonIgnore
    val spans: Map<Annotation, Sequence<TermSpan>> by lazy {
        Annotation.entries.associateWith { annotation ->
            documents.asSequence().flatMap { document ->
                document.paragraphs.asSequence().flatMap { paragraph ->
                    paragraph.sentences.asSequence().flatMap { sentence ->
                        sentence.spans[annotation]?.asSequence() ?: emptySequence()
                    }
                }
            }
        }
    }
    @get:JsonIgnore
    val summary: LayerSummary by lazy { LayerSummary(tokens = terms.count()) }
    @get:JsonIgnore
    val preview: LayerPreview by lazy { LayerPreview(terms.take(LAYER_PREVIEW_LENGTH).toList()) }
    @get:JsonIgnore
    val terms: Sequence<Term> by lazy {
        documents.asSequence().flatMap { document ->
            document.paragraphs.asSequence().flatMap { paragraph ->
                paragraph.sentences.asSequence().flatMap { sentence ->
                    sentence.terms.asSequence()
                }
            }
        }
    }

    override fun toString(): String = documents.joinToString("\n\n") + "\n" // Unix convention EOF

    companion object {
        val EMPTY: Layer = Layer(emptyList())
    }
}

class DocumentLayer(
    val id: String,
    val paragraphs: List<ParagraphLayer>,
) {
    override fun toString(): String = paragraphs.joinToString("\n\n")
}

class ParagraphLayer(
    val id: String,
    val sentences: List<SentenceLayer>,
) {
    override fun toString(): String = sentences.joinToString("\n")
}

class SentenceLayer(
    val id: String,
    val terms: List<Term>,
    val spans: Map<Annotation, List<TermSpan>>,
) {
    override fun toString(): String = terms.joinToString("") { it.token + (if (it.spaceAfter == false) "" else " ") }
}
