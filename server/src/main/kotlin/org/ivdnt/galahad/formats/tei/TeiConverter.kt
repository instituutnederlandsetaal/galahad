package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import org.ivdnt.galahad.util.XmlUtil
import org.w3c.dom.Element
import java.io.OutputStream
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class TeiConverter(export: DocumentExport) : LayerConverter(export) {
    // TODO add metadata
    override fun convert(out: OutputStream) {
        // generate an XML document from the annotation layer and write it to the output stream
        val xml = XmlUtil.builder.newDocument()
        val root = xml.createElement("TEI").apply {
            setAttribute("xmlns", "http://www.tei-c.org/ns/1.0")
            setAttribute("xmlns:tei", "http://www.tei-c.org/ns/1.0")
            setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema")
        }
        xml.appendChild(root)

        // add metadata
        TeiMetadata(xml, root, export, merging = false)

        export.layer.documents.forEach { doc ->
            val textElem = xml.createElement("text").apply { setAttribute("xml:id", doc.id) }
            root.appendChild(textElem)
            val bodyElem = xml.createElement("body")
            textElem.appendChild(bodyElem)

            doc.paragraphs.forEach { paragraph ->
                val parElem = xml.createElement("p").apply { setAttribute("xml:id", paragraph.id) }
                bodyElem.appendChild(parElem)

                paragraph.sentences.forEach { sentence ->
                    val sentElem = xml.createElement("s").apply { setAttribute("xml:id", sentence.id) }
                    parElem.appendChild(sentElem)

                    sentence.terms.forEach { t ->
                        val el: Element
                        if (t.pos == "PC" && !t.token.contains(alphaNumeric)) {
                            el = xml.createElement("pc").apply { setAttribute("xml:id", t.id) }
                        } else {
                            el = xml.createElement("w").apply { setAttribute("xml:id", t.id) }
                            t.lemma?.let { el.setAttribute("lemma", it) }
                            t.pos?.let { el.setAttribute("pos", it) }
                        }
                        el.appendChild(xml.createTextNode(t.token))
                        if (t.spaceAfter == false) {
                            el.setAttribute("join", "right")
                        }
                        sentElem.appendChild(el)
                    }
                }
            }
        }

        XmlUtil.transformer.transform(DOMSource(root), StreamResult(out))
    }

    companion object {
        private val alphaNumeric = Regex("""[a-zA-Z0-9]""")
    }
}