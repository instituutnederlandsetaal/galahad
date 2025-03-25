package org.ivdnt.galahad.annotations

const val SOURCE_LAYER_NAME: String = "sourceLayer"

/**
 * A Annotation Layer may contain multiple documents. (E.g. connlu "newdoc id", tei "<text>")
 * Those may be split into paragraphs, sentences, etc.
 */
class Layer(
    val documents: List<DocumentLayer>,
    val name: String = SOURCE_LAYER_NAME,
) {
    val summary: LayerSummary by lazy { LayerSummary(tokens = terms.count()) }
    val preview: LayerPreview by lazy { LayerPreview(terms.take(LAYER_PREVIEW_LENGTH).toList()) }
    val terms: Sequence<Term> by lazy {
        documents.asSequence().flatMap { document ->
            document.paragraphs.asSequence().flatMap { paragraph ->
                paragraph.sentences.asSequence().flatMap { sentence ->
                    sentence.terms.asSequence()
                }
            }
        }
    }
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

    override fun toString(): String = StringBuilder().apply { documents.forEach { append("$it\n") } }.toString()

    companion object {
        val EMPTY: Layer = Layer(emptyList(), "EMPTY")
    }
}

class DocumentLayer(
    val id: String,
    val paragraphs: List<ParagraphLayer>,
) {
    override fun toString(): String = StringBuilder().apply { paragraphs.forEach { append("$it\n") } }.toString()
}

class ParagraphLayer(
    val id: String,
    val sentences: List<SentenceLayer>,
) {
    override fun toString(): String = StringBuilder().apply { sentences.forEach { append("$it\n") } }.toString()
}

class SentenceLayer(
    val id: String,
    val terms: List<Term>,
    val spans: Map<Annotation, List<TermSpan>>,
) {
    override fun toString(): String = StringBuilder().apply {
        terms.forEach { append(it.token); if (it.spaceAfter) append(" ") }
    }.toString()
}
