package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.annotations.AnnotationLayer
import org.ivdnt.galahad.annotations.WordForm
import org.ivdnt.galahad.formats.AnnotationReader
import org.ivdnt.galahad.util.getXmlBuilder
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File

class TeiReader(
    file: File
) : AnnotationReader(file) {
    private val doc: Document by lazy { getXmlBuilder().parse(file) }
    private var literal: String = ""
    private var wID: String = ""

    override fun read(): AnnotationLayer {
        parseTopLevelTextNodes(doc.documentElement)
        return AnnotationLayer(documents)
    }

    /**
     * Recursively enter each node and if it is top level <text> node,
     * i.e. a <text> node that is not contained in another <text> node, parse it.
     */
    private fun parseTopLevelTextNodes(node: Node) {
        val children = node.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child.nodeType == Node.ELEMENT_NODE && (child as Element).tagName == "text") {
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

    /**
     * Parse a <text> node and its children into an AnnotationLayer.
     */
    private fun parseNodesIntoDocument(node: Node) {
        val children = node.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child.nodeType == Node.ELEMENT_NODE) {
                val tag = (child as Element).tagName
                if (IGNORABLE_TAGS.contains(tag)) {
                    continue
                }
                parseNodesIntoDocument(child)
                val id = child.getAttribute("xml:id")
                if (PARAGRAPH_TAGS.contains(tag)) {
                    // New paragraph
                    parID = id
                    newParagraph()
                } else if (SENTENCE_TAGS.contains(tag)) {
                    // New sentence
                    sentID = id
                    newSentence()
                } else if (tag == "w" || tag == "pc") {
                    // New wordform
                    wID = id
                    newWordform(child.nextSibling?.nodeName != "pc")
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

    override fun newSentence() {
        newWordform()
        super.newSentence()
    }

    private fun newWordform(spaceAfter: Boolean = true) {
        if (literal.isBlank()) return
        wordforms.add(WordForm(literal, offset, literal.length, wID, spaceAfter))
        offset += literal.length
        literal = ""
    }

    companion object {
        // TODO, which id to use? The deepest or the first?
        private val PARAGRAPH_TAGS = listOf(
            "text",
            "body",
            "front",
            "back",
            "head",
            "opener",
            "signed",
            "closer",
            "postscript",
            "signed",
            "trailer",
            "argument",
            "byline",
            "dateline",
            "docAuthor",
            "docDate",
            "epigraph",
            "meeting",
            "salute",
            "div",
            "div1",
            "div2",
            "div3",
            "div4",
            "div5",
            "div6",
            "div7",
            "lg",
            "ab",
            "p",
            "cit",
            "quote",
            "floatingText",
            "said"
        )
        private val SENTENCE_TAGS = listOf("s", "l", "u")
        private val IGNORABLE_TAGS = listOf("note")
    }
}