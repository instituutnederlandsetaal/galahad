package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

const val SOURCE_LAYER_NAME: String = "sourceLayer"

/**
 * A Annotation Layer may contain multiple documents. (E.g. connlu "newdoc id", tei "<text>")
 * Those may be split into paragraphs, sentences, etc.
 */
class Layer(
    val documents: Array<DocumentLayer>,
    val id: String = UUID.randomUUID().toString(),
) {
    @get:JsonIgnore
    val spans: Map<Annotation, Sequence<TermSpan>> by lazy {
        Annotation.entries.associateWith { annotation ->
            documents.asSequence().flatMap { document ->
                document.paragraphs.asSequence().flatMap { paragraph ->
                    paragraph.sentences.asSequence().flatMap { sentence ->
                        sentence.spans?.get(annotation)?.asSequence() ?: emptySequence()
                    }
                }
            }
        }
    }

    @get:JsonIgnore
    val summary: LayerSummary by lazy { LayerSummary(terms.asIterable()) }

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
        val EMPTY: Layer = Layer(emptyArray(), "")
    }
}

class DocumentLayer(
    val id: String,
    val paragraphs: Array<ParagraphLayer>,
) {
    override fun toString(): String = paragraphs.joinToString("\n\n")
}

class ParagraphLayer(
    val id: String,
    val sentences: Array<SentenceLayer>,
) {
    override fun toString(): String = sentences.joinToString("\n")
}

class SentenceLayer(
    val id: String,
    val terms: Array<Term>,
    spans: Map<Annotation, Array<TermSpan>>?,
) {
    val spans: Map<Annotation, Array<TermSpan>>? = spans?.ifEmpty { null }

    override fun toString(): String = terms.toSpacedString()
}
