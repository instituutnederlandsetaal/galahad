package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.util.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File

class TeiReader(
    file: File
) : AnnotationReader(file) {
    private val doc: Document by lazy { getXmlBuilder().parse(file) }
    private var literal: String = ""

    override fun read(): Layer {
        parseTopLevelTextNodes(doc.documentElement)
        return Layer(documents)
    }

    /**
     * Recursively enter each node and if it is top level <text> node,
     * i.e. a <text> node that is not contained in another <text> node, parse it.
     */
    private fun parseTopLevelTextNodes(node: Node) {
        node.childElements.forEach { child ->
            if (child.tagName == "text") {
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
     * Parse a <text> node and its children into an Layer.
     */
    private fun parseNodesIntoDocument(node: Node) {
        node.children.forEach { child ->
            if (child.nodeType == Node.ELEMENT_NODE && !IGNORABLE_TAGS.contains((child as Element).tagName)) {
                val tag = child.tagName
                val id = child.getAttribute("xml:id")

                // handle text outside of a paragraph/sentence when we are currently at a new <p>/<s>.
                // E.g.: <text> blabla <p> blabla </p> blabla </text>
                newSentenceOrParagraph(tag, id)

                // recurse
                if (tag == "subst") { // TODO should it not be sufficient to add <del> to IGNORABLE_TAGS?
                    child.childOrNull("add")?.textContent?.let { literal += it }
                } else {
                    parseNodesIntoDocument(child)
                }

                // create paragraph/sentence/word from the recursed text
                newSentenceOrParagraph(tag, id)
                if (tag == "w" || tag == "pc") {
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

    private fun newSentenceOrParagraph(tag: String, id: String?) {
        if (PARAGRAPH_TAGS.contains(tag)) {
            // New paragraph
            parID = id
            newParagraph()
        } else if (SENTENCE_TAGS.contains(tag)) {
            // New sentence
            sentID = id
            newSentence()
        }
    }

    override fun newSentence() {
        newWordform()
        super.newSentence()
    }

    private fun newWordform(el: Element? = null) {
        if (literal.isBlank()) return

        val annotations = mutableMapOf<Annotation, String>()
        el?.getAttribute("lemma")?.ifBlank { null }?.let { annotations[Annotation.LEMMA] = it }
        el?.getAttribute("pos")?.ifBlank { el.getAttribute("type").ifBlank { null } }
            ?.let { annotations[Annotation.POS] = it }
        // overwrite pos if PC
        if (el?.tagName == "pc") {
            annotations[Annotation.POS] = "PC"
        }
        annotations[Annotation.TOKEN] = literal

        val term = Term(wordID!!, offset, annotations, spaceAfter(el))
        terms.add(term)
        offset += literal.length
        literal = ""
    }

    /**
     * No space after if:
     * - join="right" or "both" on this element
     * - No space between this and the next element (inline xml)
     * - join="left" or "both" on the next element, skipping any next text nodes
     *
     * Else, space after.
     */
    private fun spaceAfter(el: Element?): Boolean {
        // join="right" or "both" on this element
        val join = el?.getAttribute("join")
        val joins = listOf("right", "both")
        if (join in joins) {
            return false
        }

        // No space between this and the next element (inline xml)
        if (el?.nextSibling?.nodeType == Node.ELEMENT_NODE) {
            return false
        }

        // join="left" or "both" on the next element, skipping any next text nodes
        val nextEl = el?.nextElementSibling()
        val nextJoin = nextEl?.getAttribute("join")
        val nextJoins = listOf("left", "both")
        return nextJoin !in nextJoins

        // space after
    }

    companion object {
        private val PARAGRAPH_TAGS = listOf(
            "text", // top most <text> defines a document, any other <text> is treated as a paragraph
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
            "quote", // TODO should <quote> be a paragraph?
            "floatingText",
            "said"
        )
        private val SENTENCE_TAGS = listOf("s", "l", "u")
        private val IGNORABLE_TAGS = listOf("note", "listBibl", "listWit", "figure")
    }
}