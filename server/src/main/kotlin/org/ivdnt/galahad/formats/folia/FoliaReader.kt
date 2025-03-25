package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.util.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File

class FoliaReader(
    file: File,
) : AnnotationReader(file) {
    val xml: Document = getXmlBuilder().parse(file)
    private var literal: String = ""
    private var pos: String = ""
    private var lemma: String = ""

    override fun read(): Layer {
        parseTopLevelTextNodes(xml.documentElement)
        return Layer(documents)
    }

    private fun parseTopLevelTextNodes(node: Node) {
        node.childElements.forEach { child ->
            if (child.tagName == "text" || child.tagName == "speech") {
                // parse document
                parseNodesIntoDocument(child)
                docID = child.getAttribute("xml:id")
                newDocument()
            } else {
                // recurse
                parseTopLevelTextNodes(child)
            }
        }
    }

    private fun parseNodesIntoDocument(node: Node) {
        node.children.forEach { child ->
            if (child.nodeType == Node.ELEMENT_NODE && !IGNORABLE_TAGS.contains((child as Element).tagName)) {
                val tag = child.tagName
                val id = child.getAttribute("xml:id")

                // recurse
                parseNodesIntoDocument(child)

                // create paragraph/sentence/word from the recursed text
                if (PARAGRAPH_TAGS.contains(tag)) {
                    // New paragraph
                    parID = id
                    newParagraph()
                } else if (SENTENCE_TAGS.contains(tag)) {
                    // New sentence
                    sentID = id
                    newSentence()
                } else if (tag == "pos") {
                    pos = child.getAttribute("class")
                } else if (tag == "lemma") {
                    lemma = child.getAttribute("class")
                } else if (tag == "w") {
                    // New wordform
                    wordID = id
                    newWordform(child)
                }

            } else if (child.nodeType == Node.TEXT_NODE) {
                val text = child.textContent
                val words = text.split("\\s+".toRegex())
                for ((j, word) in words.withIndex()) {
                    if (j > 0) {
                        newWordform()
                    }
                    literal += word
                }
            }
        }
    }

    private fun newWordform(el: Element? = null) {
        if (literal.isBlank()) return

        val annotations = mutableMapOf<Annotation, String>()
        lemma.takeIf { it.isNotBlank() }?.let { annotations[Annotation.LEMMA] = it }
        pos.takeIf { it.isNotBlank() }?.let { annotations[Annotation.POS] = it }
        annotations[Annotation.TOKEN] = literal

        val term = Term(wordID!!, offset, annotations, el?.getAttribute("space") != "no")
        terms.add(term)
        offset += literal.length
        literal = ""
    }

    companion object {
        private val PARAGRAPH_TAGS = listOf(
            "text", // top most <text> defines a document, any other <text> is treated as a paragraph
            "speech", // same for speech
            "div",
            "p",
            "head",
            "list",
            "item",
            "event",
            "table",
            "part",
        )
        private val SENTENCE_TAGS = listOf("s", "utt")
        private val IGNORABLE_TAGS = listOf(
            "note", "figure", "comment",
            "original", // correction
            "suggestion", // correction

        )
    }
}