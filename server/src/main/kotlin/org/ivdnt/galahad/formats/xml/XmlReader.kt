package org.ivdnt.galahad.formats.xml

import org.ivdnt.galahad.annotations.*
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.util.XmlUtil
import java.io.InputStream
import java.util.*
import javax.xml.XMLConstants
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

abstract class XmlReader(stream: InputStream) : AnnotationReader() {
    protected var pos: String? = null
    protected var lemma: String? = null
    protected var literal: String = ""
    protected var spaceAfter: Boolean = true

    protected var nerValue: String? = null
    protected var nerTargets: MutableList<String> = mutableListOf()

    protected var deprel: String? = null
    protected var deprelFrom: String? = null
    protected var deprelTo: String? = null


    protected val reader: XMLStreamReader by lazy { XmlUtil.inputFactory.createXMLStreamReader(stream) }

    private val currentXmlID: String?
        get() = reader.getAttributeValue(XMLConstants.XML_NS_URI, "id")?.ifBlank { null }
    private var ignoring: Boolean = false
    private var currentDepth: Int = 0
    private var ignoreDepth: Int? = null

    abstract val nerTags: Array<String>
    abstract val documentTags: Array<String>
    abstract val paragraphTags: Array<String>
    abstract val sentenceTags: Array<String>
    abstract val wordTags: Array<String>
    abstract val ignorableTags: Array<String>
    abstract val depTags: Array<String>

    /** Path of the current XML element */
    protected val xmlPath: MutableList<String> = mutableListOf()

    final override fun read(): Layer {
        // retrieve the XML ID of the document root
        var rootID: String = UUID.randomUUID().toString()
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> {
                    currentXmlID?.let { rootID = it }
                    break
                }
            }
        }
        parseDocuments()
        return Layer(documents.toTypedArray(), rootID)
    }

    private fun parseDocuments() {
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> if (!shouldIgnore()) {
                    parseAttrs()
                    when (reader.localName) {
                        in documentTags -> docID = currentXmlID
                        in paragraphTags -> parID = currentXmlID
                        in sentenceTags -> sentID = currentXmlID
                        in wordTags -> wordID = currentXmlID
                    }
                }
                XMLStreamConstants.CHARACTERS -> if (!ignoring) parseChars()
                XMLStreamConstants.END_ELEMENT -> if (!shouldIgnore()) {
                    when (reader.localName) {
                        in documentTags -> newDocument()
                        in paragraphTags -> newParagraph()
                        in sentenceTags -> newSentence()
                        in wordTags -> newWordform()
                        in nerTags -> newSpan()
                        in depTags -> newDep()
                    }
                }
            }
        }
    }

    private fun newDep() {
        // edit the DEPREL and HEAD value of the terms
        if (deprel != null) {
            val depI = terms.indexOfFirst { it.id == deprelTo }
            val dep = terms[depI]
            val headI = terms.indexOfFirst { it.id == deprelFrom }

            val annots = buildMap {
                putAll(dep.annotations)
                put(Annotation.DEPREL, deprel)
                put(Annotation.HEAD, (headI + 1).toString())
            }

            terms[depI] = Term(
                dep.id,
                dep.offset,
                annots,
                dep.spaceAfter
            )

            // reset
            deprel = null
            deprelFrom = null
            deprelTo = null
        }
    }

    override fun newSentence() {
        // edit the NER value of the terms if spans are present
        spans[Annotation.NER]?.forEach { span ->
            span.indices.forEachIndexed { spanI, termI ->
                // Note the difference spanI and termI; e.g. span.indices = [4, 5]; so (0, 4) = (1, 5)
                val t = terms[termI]
                val iob = (if (spanI == 0) "B-" else "I-") + span.value
                terms[termI] = Term(t.id, t.offset, t.annotations + (Annotation.NER to iob), t.spaceAfter)
            }
        }
        // if any DEPREL is present, set the root term to ROOT (i.e. the term with no deprel)
        if (terms.any { it.deprel != null }) {
            val rootI = terms.indexOfFirst { it.deprel == null }
            val root = terms[rootI]
            val annots = buildMap {
                putAll(root.annotations)
                put(Annotation.DEPREL, "root")
                put(Annotation.HEAD, "0")
            }
            terms[rootI] = Term(root.id, root.offset, annots, root.spaceAfter)
        }
        super.newSentence()
        nerTargets.clear()
    }

    private fun newSpan() {
        if (nerValue == null) return
        val indices = nerTargets.map { id -> terms.indexOfFirst { t -> t.id == id } }
        spans.getOrPut(Annotation.NER, ::mutableListOf) += TermSpan(indices, nerValue!!)
        nerValue = null
        nerTargets.clear()
    }

    protected abstract fun parseAttrs()

    private fun shouldIgnore(): Boolean {
        if (reader.isStartElement) {
            currentDepth++
            xmlPath.add(reader.localName)
            if (!ignoring && reader.localName in ignorableTags) {
                ignoring = true
                ignoreDepth = currentDepth
            }
        } else if (reader.isEndElement) {
            if (currentDepth == ignoreDepth) {
                ignoring = false
                ignoreDepth = null
            }
            currentDepth--
            xmlPath.removeLastOrNull()
        }
        return ignoring
    }

    private fun parseChars() {
        val words = reader.text.ifBlank { null }?.split(whitespace) ?: emptyList()
        for ((j, word) in words.withIndex()) {
            if (j > 0) newWordform()
            literal += word
        }
    }

    override fun newWordform() {
        if (literal.isBlank()) return
        val annotations = buildMap {
            lemma?.ifBlank { null }?.let { put(Annotation.LEMMA, it) }
            pos?.ifBlank { null }?.let { put(Annotation.POS, it) }
            put(Annotation.TOKEN, literal)
        }
        terms += Term(wordID(), offset, annotations, spaceAfter)
        offset += literal.length
        if (spaceAfter) offset++
        literal = ""
        lemma = null
        pos = null
    }

    companion object {
        private val whitespace: Regex = Regex("""\s+""")
    }
}
