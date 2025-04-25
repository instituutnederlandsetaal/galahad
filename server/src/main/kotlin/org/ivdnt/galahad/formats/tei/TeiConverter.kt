package org.ivdnt.galahad.formats.tei

import org.codehaus.stax2.XMLStreamWriter2
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.TermSpan
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import org.ivdnt.galahad.formats.xml.PrettyXMLWriter
import org.ivdnt.galahad.util.XmlUtil.Companion.outputFactory
import java.io.OutputStream
import javax.xml.XMLConstants

class TeiConverter(export: DocumentExport) : LayerConverter(export) {
    override fun convert(out: OutputStream) {
        val writer = PrettyXMLWriter(outputFactory.createXMLStreamWriter(out) as XMLStreamWriter2)

        writer.writeStartDocument("UTF-8", "1.0")
        writer.writeStartElement("TEI")
        writer.writeNamespace("", "http://www.tei-c.org/ns/1.0")
        writer.writeNamespace("xml", "http://www.w3.org/XML/1998/namespace")
        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", export.layer.id)
        //TeiMetadata(writer, export, merging = false)

        export.layer.documents.forEach { doc ->
            writer.writeStartElement("text")
            writer.writeAttribute(XMLConstants.XML_NS_URI, "id", doc.id)

            writer.writeStartElement("body")

            doc.paragraphs.forEach { paragraph ->
                writer.writeStartElement("p")
                writer.writeAttribute(XMLConstants.XML_NS_URI, "id", paragraph.id)

                paragraph.sentences.forEach { sentence ->
                    writer.writeStartElement("s")
                    writer.writeAttribute(XMLConstants.XML_NS_URI, "id", sentence.id)
                    val ners = sentence.spans[Annotation.NER]
                    sentence.terms.forEachIndexed { termI, t ->
                        // if the term is in a span, we output:
                        // <name type="ORG">
                        //     <w>...</w>
                        // </name>
                        ners?.firstOrNull<TermSpan> { termI == it.indices.first<Int>() }?.let {
                            writer.writeStartElement("name")
                            writer.writeAttribute("type", it.value)
                        }

                        val tag = if (t.pos == "PC" && !t.token.contains(alphaNumeric)) "pc" else "w"
                        writer.writeStartElement(tag)
                        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", t.id)
                        if (tag == "w") {
                            t.lemma?.let { writer.writeAttribute("lemma", it) }
                        }
                        t.pos?.let { writer.writeAttribute("pos", it) }
                        if (t.spaceAfter == false) writer.writeAttribute("join", "right")
                        writer.writeCharacters(t.token)
                        writer.writeEndElement()
                        if (ners?.any { termI == it.indices.last() } == true) {
                            writer.writeEndElement() // name
                        }

                    }
                    writer.writeEndElement() // s

                }
                writer.writeEndElement() // p

            }
            writer.writeEndElement() // body
            writer.writeEndElement() // text
        }
        writer.writeEndElement() // TEI
        writer.writeEndDocument()
        writer.flush()
        writer.close()
    }

    companion object {
        private val alphaNumeric = Regex("""[a-zA-Z0-9]""")
    }
}
