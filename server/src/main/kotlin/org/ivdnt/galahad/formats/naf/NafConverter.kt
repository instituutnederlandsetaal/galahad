package org.ivdnt.galahad.formats.naf

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import org.ivdnt.galahad.util.XmlUtil
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.OutputStream
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class NafConverter(export: DocumentExport) : LayerConverter(export) {
    val now: Long = System.currentTimeMillis()

    override fun convert(out: OutputStream) {
        val xml = XmlUtil.builder.newDocument()
        val root = xml.createElement("NAF").apply {
            setAttribute("version", "v3.3")
            setAttribute("xml:lang", "dum")
        }
        xml.appendChild(root)

        addNafHeader(xml, root)
        addRaw(xml, root)
        addText(xml, root)
        addTerms(xml, root)

        XmlUtil.transformer.transform(DOMSource(root), StreamResult(out))
    }

    private fun addTerms(xml: Document, root: Element) {
        val terms = xml.createElement("terms")
        root.appendChild(terms)
        export.layer.terms.forEachIndexed { i, it ->
            val term = xml.createElement("term").apply {
                setAttribute("id", "t$i")
            }
            it.lemma?.let { term.setAttribute("lemma", it) }
            it.pos?.let { term.setAttribute("pos", it) }
            terms.appendChild(term)

            // target span
            val target = xml.createElement("target").apply {
                setAttribute("id", it.id)
            }
            val span = xml.createElement("span").apply {
                appendChild(target)
            }
            term.appendChild(span)
        }
    }

    private fun addText(xml: Document, root: Element) {
        val text = xml.createElement("text")
        root.appendChild(text)
        val paragraphs = export.layer.documents.flatMap { it.paragraphs.asSequence() }
        var iSent = 1
        paragraphs.forEachIndexed { iPar, paragraph ->
            paragraph.sentences.forEach { sentence ->
                sentence.terms.forEach { t ->
                    val wf = xml.createElement("wf").apply {
                        setAttribute("id", t.id)
                        setAttribute("offset", t.offset.toString())
                        setAttribute("length", t.token.length.toString())
                        setAttribute("sent", iSent.toString())
                        setAttribute("para", iPar.toString())
                        textContent = t.token
                    }
                    text.appendChild(wf)
                }
                iSent++
            }
        }
    }

    private fun addRaw(xml: Document, root: Element) {
        val cdata = xml.createCDATASection(export.layer.toString())
        val raw = xml.createElement("raw")
        raw.appendChild(cdata)
        root.appendChild(raw)
    }

    private fun addNafHeader(xml: Document, root: Element) {
        val nafHeader = xml.createElement("nafHeader")
        root.appendChild(nafHeader)

        val fileDesc = xml.createElement("fileDesc").apply {
            setAttribute("title", export.document.name)
            setAttribute("author", export.user.id)
            setAttribute("creationtime", now.toString())
            setAttribute("filename", export.document.name)
            setAttribute("filetype", export.document.metadata.format.identifier)
        }
        nafHeader.appendChild(fileDesc)
        val public = xml.createElement("public").apply {
            setAttribute("publicId", export.document.metadata.uuid.toString())
        }
        nafHeader.appendChild(public)

        val lp = xml.createElement("lp").apply {
            setAttribute("name", export.tagger.id)
            setAttribute("version", export.tagger.version)
            setAttribute("timestamp", now.toString())
            setAttribute("beginTimestamp", now.toString())
            setAttribute("endTimestamp", now.toString())
            setAttribute("hostname", "https://galahad.ivdnt.org")
        }
        val lpTerms = xml.createElement("linguisticProcessors").apply {
            setAttribute("layer", "terms")
        }
        lpTerms.appendChild(lp)
        nafHeader.appendChild(lpTerms)

        if (Annotation.NER in export.tagger.annotations) {
            val lpNer = xml.createElement("linguisticProcessors").apply {
                setAttribute("layer", "entities")
            }
            lpNer.appendChild(lp.cloneNode(true))
            nafHeader.appendChild(lpNer)
        }

        if (Annotation.DEPREL in export.tagger.annotations) {
            val lpDep = xml.createElement("linguisticProcessors").apply {
                setAttribute("layer", "deps")
            }
            lpDep.appendChild(lp.cloneNode(true))
            nafHeader.appendChild(lpDep)
        }
    }
}