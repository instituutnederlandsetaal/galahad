package org.ivdnt.galahad.formats.xml

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.util.XmlUtil
import java.io.InputStream
import javax.xml.XMLConstants
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

abstract class XmlReader(stream: InputStream) : AnnotationReader() {
    protected var pos: String = ""
    protected var lemma: String = ""
    protected var literal: String = ""
    protected var spaceAfter: Boolean = true
    protected val reader: XMLStreamReader by lazy { XmlUtil.inputFactory.createXMLStreamReader(stream) }
    private val currentXmlID: String?
        get() = reader.getAttributeValue(XMLConstants.XML_NS_URI, "id")?.takeIf { it.isNotBlank() }
    private var ignoring: Boolean = false
    abstract val documentTags: Array<String>
    abstract val paragraphTags: Array<String>
    abstract val sentenceTags: Array<String>
    abstract val wordTags: Array<String>
    abstract val wordDataTags: Array<String>
    abstract val ignorableTags: Array<String>

    final override fun read(): Layer {
        parseDocuments()
        return Layer(documents.toTypedArray())
    }

    private fun parseDocuments() {
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> if (reader.localName in documentTags) {
                    setID()
                    parseNodesInDocument()
                    newDocument()
                }
            }
        }
    }

    private fun parseNodesInDocument() {
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> if (!shouldIgnore()) {
                    setID()
                    when (reader.localName) {
                        in paragraphTags -> newParagraph()
                        in sentenceTags -> newSentence()
                        in wordDataTags -> parseWordData()
                    }
                }
                XMLStreamConstants.CHARACTERS -> if (!ignoring) parseChars()
                XMLStreamConstants.END_ELEMENT -> if (!ignoring && reader.localName in wordTags) newWordform()
            }
        }
    }

    private fun setID() {
        when (reader.localName) {
            in documentTags -> docID = currentXmlID
            in paragraphTags -> parID = currentXmlID
            in sentenceTags -> sentID = currentXmlID
            in wordTags -> wordID = currentXmlID
        }
    }

    protected abstract fun parseWordData()

    private fun shouldIgnore(): Boolean = ignoring.also { ignoring = reader.localName in ignorableTags }

    private fun parseChars() {
        val words = reader.text.takeIf { it.isNotBlank() }?.split(whitespace) ?: emptyList()
        for ((j, word) in words.withIndex()) {
            if (j > 0) newWordform()
            literal += word
        }
    }

    override fun newSentence() {
        newWordform()
        super.newSentence()
    }

    private fun newWordform() {
        if (literal.isBlank()) return
        val annotations = buildMap {
            lemma.takeIf { it.isNotBlank() }?.let { put(Annotation.LEMMA, it) }
            pos.takeIf { it.isNotBlank() }?.let { put(Annotation.POS, it) }
            put(Annotation.TOKEN, literal)
        }
        terms += Term(wordID(), offset, annotations, spaceAfter)
        offset += literal.length
        literal = ""
    }

    companion object {
        private val whitespace: Regex = Regex("""\s+""")
    }
}
