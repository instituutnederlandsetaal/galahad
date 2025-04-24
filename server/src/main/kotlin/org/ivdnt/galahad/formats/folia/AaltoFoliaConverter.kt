package org.ivdnt.galahad.formats.folia

import org.codehaus.stax2.XMLStreamWriter2
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.TermSpan
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import org.ivdnt.galahad.formats.xml.PrettyXMLStreamWriter
import org.ivdnt.galahad.util.XmlUtil.Companion.outputFactory
import java.io.OutputStream
import javax.xml.XMLConstants

class AaltoFoliaConverter(export: DocumentExport) : LayerConverter(export) {
    override fun convert(out: OutputStream) {
        val writer = PrettyXMLStreamWriter(outputFactory.createXMLStreamWriter(out) as XMLStreamWriter2)

        writer.writeStartDocument("UTF-8", "1.0")
        writer.writeStartElement("FoLiA")
        writer.writeNamespace("", "http://ilk.uvt.nl/folia")
        writer.writeNamespace("xml", "http://www.w3.org/XML/1998/namespace")
        writer.writeAttribute("", "generator", "galahad.ivdnt.org")
        writer.writeAttribute("", "version", "2.5.3")
        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", export.layer.id)

        documents.forEach { doc ->
            writer.writeStartElement("text")
            writer.writeAttribute(XMLConstants.XML_NS_URI, "id", doc.id)

            doc.paragraphs.forEach { paragraph ->
                writer.writeStartElement("p")
                writer.writeAttribute(XMLConstants.XML_NS_URI, "id", paragraph.id)

                paragraph.sentences.forEach { sentence ->
                    writer.writeStartElement("s")
                    writer.writeAttribute(XMLConstants.XML_NS_URI, "id", sentence.id)

                    sentence.terms.forEachIndexed { termI, t ->
                        val wClass = if (t.pos == "PC" && !t.token.contains(alphaNumeric)) "PUNCTUATION" else "WORD"

                        writer.writeStartElement("w")
                        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", t.id)
                        writer.writeAttribute("class", wClass)
                        if (t.spaceAfter == false) writer.writeAttribute("space", "no")

                        writer.writeStartElement("t")
                        writer.writeCharacters(t.token)
                        writer.writeEndElement()

                        if (wClass == "WORD" && t.lemma != null) {
                            writer.writeStartElement("lemma")
                            writer.writeAttribute("class", t.lemma)
                            writer.writeEndElement(indent = false)
                        }

                        if (t.pos != null) {
                            writer.writeStartElement("pos")
                            writer.writeAttribute("class", t.pos)
                            writer.writeAttribute("head", t.annotationHead(Annotation.POS))
                            writer.writeEndElement(indent = false)
                        }

                        writer.writeEndElement() // w
                    }

                    sentence.spans[Annotation.NER]?.let { nerSpans ->
                        writer.writeStartElement("entities")
                        nerSpans.forEachIndexed { spanI, span ->
                            writer.writeStartElement("entity")
                            val spanId = "${sentence.id}.e${spanI + 1}"
                            writer.writeAttribute(XMLConstants.XML_NS_URI, "id", spanId)
                            writer.writeAttribute("class", span.value)
                            span.indices.forEach { termI ->
                                val term = sentence.terms[termI]
                                writer.writeStartElement("wref")
                                writer.writeAttribute("id", term.id)
                                writer.writeAttribute("t", term.token)
                                writer.writeEndElement(indent = false)
                            }
                            writer.writeEndElement() // entity
                        }
                        writer.writeEndElement() // entities
                    }

                    writer.writeEndElement() // s
                }
                writer.writeEndElement() // p
            }
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
