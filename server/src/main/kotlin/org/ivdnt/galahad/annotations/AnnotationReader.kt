package org.ivdnt.galahad.annotations

abstract class AnnotationReader {
    val layer: Layer by lazy { read() }

    protected val documents: MutableList<DocumentLayer> = mutableListOf()
    protected val paragraphs: MutableList<ParagraphLayer> = mutableListOf()
    protected val sentences: MutableList<SentenceLayer> = mutableListOf()
    protected val terms: MutableList<Term> = mutableListOf()
    protected val spans: MutableMap<Annotation, Array<TermSpan>> = mutableMapOf()

    protected var offset: Int = 0

    protected var docID: String? = null
    protected var parID: String? = null
    protected var sentID: String? = null
    protected var wordID: String? = null

    private fun docID(): String = docID ?: "d$dIndex"
    private fun parID(): String = parID ?: "p$pIndex"
    private fun sentID(): String = sentID ?: "s$sIndex"
    protected fun wordID(): String = wordID ?: "w$wIndex"

    private val wIndex: Int get() = terms.size + 1
    private val sIndex: Int get() = sentences.size + 1
    private val pIndex: Int get() = paragraphs.size + 1
    private val dIndex: Int get() = documents.size + 1

    protected abstract fun read(): Layer

    protected open fun newDocument() {
        newParagraph()
        if (paragraphs.isNotEmpty()) {
            documents.add(DocumentLayer(docID(), paragraphs.toTypedArray()))
            paragraphs.clear()
        }
    }

    protected open fun newParagraph() {
        newSentence()
        if (sentences.isNotEmpty()) {
            paragraphs.add(ParagraphLayer(parID(), sentences.toTypedArray()))
            sentences.clear()
        }
    }

    protected open fun newSentence() {
        if (terms.isNotEmpty()) {
            sentences.add(SentenceLayer(sentID(), terms.toTypedArray(), spans.toMap()))
            terms.clear()
            spans.clear()
        }
    }
}