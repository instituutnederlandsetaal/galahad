package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.util.XmlUtil
import java.io.File
import javax.xml.XMLConstants
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

class AaltoFoliaReader(file: File,
) : AnnotationReader(file)  {
    private var pos: String = ""
    private var lemma: String = ""
    private var literal: String = ""
    private var ignoring: Boolean = false
    private val reader: XMLStreamReader by lazy { XmlUtil.inputFactory.createXMLStreamReader(file.inputStream()) }

    override fun read(): Layer {
        parseTopLevelTextNodes()
        return Layer(documents.toTypedArray())
    }

    private fun parseTopLevelTextNodes() {
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> {
                    val tagName = reader.localName
                    if (tagName == "text" || tagName == "speech") {
                        docID = reader.getAttributeValue(XMLConstants.XML_NS_URI, "id")?.takeIf { it.isNotBlank() }
                        parseNodesIntoDocument()
                        newDocument()
                    }
                }
            }
        }
    }

    private fun parseNodesIntoDocument() {
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> {
                    val tag = reader.localName

                    if (IGNORABLE_TAGS.contains(tag) || ignoring) {
                        ignoring = true
                        continue
                    }

                    val id = reader.getAttributeValue(XMLConstants.XML_NS_URI, "id")?.takeIf { it.isNotBlank() }

                    if (PARAGRAPH_TAGS.contains(tag)) {
                        parID = id
                        newParagraph()
                    } else if (SENTENCE_TAGS.contains(tag)) {
                        sentID = id
                        newSentence()
                    } else if (tag == "pos") {
                        pos = reader.getAttributeValue(null, "class") ?: ""
                    } else if (tag == "lemma") {
                        lemma = reader.getAttributeValue(null, "class") ?: ""
                    } else if (tag == "w") {
                        wordID = id
                        newWordform()
                    }
                }
                XMLStreamConstants.CHARACTERS -> {
                    if (ignoring) continue

                    val text = reader.text
                    if (text.isNotBlank()) {
                        val words = text.split(whitespace)
                        for ((j, word) in words.withIndex()) {
                            if (j > 0) newWordform()
                            literal += word
                        }
                    }
                }
                XMLStreamConstants.END_ELEMENT -> {
                    val tag = reader.localName
                    if (IGNORABLE_TAGS.contains(tag)) {
                        ignoring = false
                        continue
                    }
                    if (ignoring) continue
                    if (reader.localName == "w") {
                        newWordform()
                    }
                }
            }
        }
    }

    private fun newWordform() {
        if (literal.isBlank()) return

        val annotations = buildMap {
            lemma.takeIf { it.isNotBlank() }?.let { put(Annotation.LEMMA, it) }
            pos.takeIf { it.isNotBlank() }?.let { put(Annotation.POS, it) }
            put(Annotation.TOKEN, literal)
        }

        val spaceAfter = true
        terms += Term(wordID ?: "", offset, annotations, spaceAfter)
        offset += literal.length
        literal = ""
    }

    companion object {
        private val whitespace: Regex = Regex("""\s+""")
        private val PARAGRAPH_TAGS = arrayOf(
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
        private val SENTENCE_TAGS = arrayOf("s", "utt")
        private val IGNORABLE_TAGS = arrayOf(
            "morphology", "note", "figure", "comment",
            "original", // correction
            "suggestion", // correction
        )
    }
}
