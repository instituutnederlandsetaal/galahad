package org.ivdnt.galahad.formats

import org.ivdnt.galahad.annotations.*
import java.io.File

abstract class AnnotationReader(
    protected val file: File
) {
    val layer: AnnotationLayer by lazy { read() }

    protected val documents: MutableList<DocumentLayer> = mutableListOf()
    protected val paragraphs: MutableList<ParagraphLayer> = mutableListOf()
    protected val sentences: MutableList<SentenceLayer> = mutableListOf()
    protected val wordforms: MutableList<WordForm> = mutableListOf()

    protected var offset: Int = 0
    protected var docID: String = "d1"
    protected var parID: String = "p1"
    protected var sentID: String = "s1"

    protected abstract fun read(): AnnotationLayer

    protected open fun newDocument() {
        newParagraph()
        if (paragraphs.isNotEmpty()) {
            documents.add(DocumentLayer(docID, paragraphs.toList()))
            paragraphs.clear()
        }
    }

    protected open fun newParagraph() {
        newSentence()
        if (sentences.isNotEmpty()) {
            paragraphs.add(ParagraphLayer(parID, sentences.toList()))
            sentences.clear()
        }
    }

    protected open fun newSentence() {
        if (wordforms.isNotEmpty()) {
            sentences.add(SentenceLayer(sentID, wordforms.toList()))
            wordforms.clear()
        }
    }
}