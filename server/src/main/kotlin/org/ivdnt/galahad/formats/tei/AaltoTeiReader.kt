package org.ivdnt.galahad.formats.tei

import org.codehaus.stax2.XMLEventReader2
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.util.XmlUtil
import java.io.BufferedInputStream
import javax.xml.XMLConstants
import javax.xml.namespace.QName
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.events.StartElement

class AaltoTeiReader(
    stream: BufferedInputStream,
) : AnnotationReader() {
    private val reader: XMLEventReader2 by lazy { XmlUtil.inputFactory.createXMLEventReader(stream) as XMLEventReader2 }
    private var literal: String = ""
    private var ignoring: Boolean = false

    override fun read(): Layer {
        parseTopLevelTextNodes()
        return Layer(documents.toTypedArray())
    }

    /**
     * Recursively enter each node and if it is top level <text> node,
     * i.e. a <text> node that is not contained in another <text> node, parse it.
     */
    private fun parseTopLevelTextNodes() {
        while (reader.hasNextEvent()) {
            val event = reader.nextEvent()
            when (event.eventType) {
                XMLStreamConstants.START_ELEMENT -> {
                    val el = event.asStartElement()
                    if (el.name.localPart == "text") {
                        docID =
                            el.getAttributeByName(QName(XMLConstants.XML_NS_URI, "id"))?.value?.takeIf { it.isNotBlank() }
                        parseNodesIntoDocument()
                        newDocument()
                    }
                }
            }
        }
    }

    /**
     * Parse a <text> node and its children into an Layer.
     */
    private fun parseNodesIntoDocument() {
        while (reader.hasNextEvent()) {
            val event = reader.nextEvent()
            when (event.eventType) {
                XMLStreamConstants.START_ELEMENT -> {
                    val e = event.asStartElement()
                    val tag = e.name.localPart

                    if (IGNORABLE_TAGS.contains(tag) || ignoring) {
                        ignoring = true
                        continue
                    }

                    val id = e.getAttributeByName(QName(XMLConstants.XML_NS_URI, "id"))?.value?.takeIf { it.isNotBlank() }

                    // handle text outside of a paragraph/sentence when we are currently at a new <p>/<s>.
                    // E.g.: <text> blabla <p> blabla </p> blabla </text>
                    newSentenceOrParagraph(tag, id)

                    if (tag == "w" || tag == "pc") {
                        wordID = id
                        val lemma = e?.getAttributeByName(QName("lemma"))?.value?.takeIf { it.isNotBlank() }
                        val pos = e?.getAttributeByName(QName("pos"))?.value?.takeIf { it.isNotBlank() } ?: e?.getAttributeByName(
                            QName(
                                "type"
                            )
                        )?.value?.takeIf { it.isNotBlank() }
                    }
                }

                XMLStreamConstants.CHARACTERS -> {
                    if (ignoring) continue
                    val e = event.asCharacters()

                    val words = e.data.trim().split(whitespace)
                    for ((j, word) in words.withIndex()) {
                        if (j > 0) newWordform()
                        literal += word
                    }
                }

                XMLStreamConstants.END_ELEMENT -> {
                    val e = event.asEndElement()

                    val tag = e.name.localPart
                    if (IGNORABLE_TAGS.contains(tag)) {
                        ignoring = false
                    }
                    if (ignoring) continue
                    if (tag == "w") {
                        newWordform()
                    }
                }
            }
        }
    }

    private fun newSentenceOrParagraph(tag: String, id: String?) {
        if (tag in PARAGRAPH_TAGS) {
            parID = id
            newParagraph()
        } else if (tag in SENTENCE_TAGS) {
            sentID = id
            newSentence()
        }
    }

    override fun newSentence() {
        newWordform()
        super.newSentence()
    }

    private fun newWordform() {
        if (literal.isBlank()) return
        val term = Term(wordID(), offset, mapOf(Annotation.TOKEN to literal))
        terms.add(term)
        offset += literal.length
        literal = ""
    }

    private fun newWordform(tag: String? = null, id: String? = null, e: StartElement? = null) {
        if (literal.isBlank()) return

        val annotations = mutableMapOf<Annotation, String>()


//        lemma?.let { annotations[Annotation.LEMMA] = it }
//        pos?.let { annotations[Annotation.POS] = it }
        if (tag == "pc") annotations[Annotation.POS] = "PC"
        annotations[Annotation.TOKEN] = literal

        terms += Term(wordID(), offset, annotations, spaceAfter(e))
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
    private fun spaceAfter(e: StartElement?): Boolean {
        // join="right" or "both" on this element
        val join = e?.getAttributeByName(QName("join"))?.value
        return join !in arrayOf("right", "both")
//        val next = reader.peek()
//        if (next.eventType ==
//        val nextEvent = reader.nextTag()
//        val nextJoin = reader.getAttributeValue(null, "join")
//        return !(nextJoin == "left" || nextJoin == "both")
    }

    companion object {
        private val whitespace: Regex = Regex("""\s+""")
        private val PARAGRAPH_TAGS = arrayOf(
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
        private val SENTENCE_TAGS = arrayOf("s", "l", "u")
        private val IGNORABLE_TAGS = arrayOf("note", "listBibl", "listWit", "figure", "xr", "fs", "del")
    }
}
