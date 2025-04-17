package org.ivdnt.galahad.formats.tei

import org.codehaus.stax2.XMLStreamWriter2
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import org.ivdnt.galahad.util.XmlUtil.Companion.outputFactory
import java.io.BufferedOutputStream
import java.io.OutputStream
import javax.xml.XMLConstants

class TeiAaltoConverter(export: DocumentExport) : LayerConverter(export) {
    override fun convert(out: OutputStream) {
        val writer = outputFactory.createXMLStreamWriter(BufferedOutputStream(out), "UTF-8") as XMLStreamWriter2

        writer.writeStartDocument("UTF-8", "1.0")
        writer.writeRaw("\n")
        writer.writeStartElement("TEI")
        writer.writeNamespace("", "http://www.tei-c.org/ns/1.0")
        writer.writeNamespace("tei", "http://www.tei-c.org/ns/1.0")
        writer.writeNamespace("xs", "http://www.w3.org/2001/XMLSchema")
        writer.writeRaw("\n")
        //TeiMetadata(writer, export, merging = false)

        export.layer.documents.forEach { doc ->
            writer.writeRaw(INDENT_TEI)
            writer.writeStartElement("text")
            writer.writeAttribute(XMLConstants.XML_NS_URI, "id", doc.id)
            writer.writeRaw("\n")

            writer.writeRaw(INDENT_TEXT)
            writer.writeStartElement("body")
            writer.writeRaw("\n")

            doc.paragraphs.forEach { paragraph ->
                writer.writeRaw(INDENT_BODY)
                writer.writeStartElement("p")
                writer.writeAttribute(XMLConstants.XML_NS_URI, "id", paragraph.id)
                writer.writeRaw("\n")

                paragraph.sentences.forEach { sentence ->
                    writer.writeRaw(INDENT_P)
                    writer.writeStartElement("s")
                    writer.writeAttribute(XMLConstants.XML_NS_URI, "id", sentence.id)
                    writer.writeRaw("\n")

                    sentence.terms.forEach { t ->
                        val tag = if (t.pos == "PC" && !t.token.contains(alphaNumeric)) "pc" else "w"
                        writer.writeRaw(INDENT_S)
                        writer.writeStartElement(tag)
                        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", t.id)
                        t.lemma?.let { writer.writeAttribute("lemma", it) }
                        t.pos?.let { writer.writeAttribute("pos", it) }
                        if (t.spaceAfter == false) writer.writeAttribute("join", "right")
                        writer.writeCharacters(t.token)
                        writer.writeEndElement()
                        writer.writeRaw("\n")
                    }
                    writer.writeRaw(INDENT_P)
                    writer.writeEndElement() // s
                    writer.writeRaw("\n")

                }
                writer.writeRaw(INDENT_BODY)
                writer.writeEndElement() // p
                writer.writeRaw("\n")

            }
            writer.writeRaw(INDENT_TEXT)
            writer.writeEndElement() // body
            writer.writeRaw("\n")
            writer.writeRaw(INDENT_TEI)
            writer.writeEndElement() // text
            writer.writeRaw("\n")
        }
        writer.writeEndElement() // TEI
        writer.writeEndDocument()
        writer.flush()
        writer.close()
    }

    companion object {
        private val alphaNumeric = Regex("""[a-zA-Z0-9]""")
        private val TAB = " ".repeat(4)
        private val INDENT_TEI = TAB
        private val INDENT_TEXT = TAB.repeat(2)
        private val INDENT_BODY = TAB.repeat(3)
        private val INDENT_P = TAB.repeat(4)
        private val INDENT_S = TAB.repeat(5)
    }
}
