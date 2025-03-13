package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.annotations.AnnotationLayer
import org.ivdnt.galahad.util.getXmlBuilder
import org.ivdnt.galahad.util.getXmlTransformer
import java.io.OutputStream
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class TeiConverter(
    private val layer: AnnotationLayer
) {
    fun convert(out: OutputStream) {
        // generate an XML document from the annotation layer and write it to the output stream
        val xml = getXmlBuilder().newDocument()
        val root = xml.createElement("TEI").apply {
            setAttribute("xmlns", "http://www.tei-c.org/ns/1.0")
            setAttribute("xmlns:tei", "http://www.tei-c.org/ns/1.0")
            setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema")
        }
        xml.appendChild(root)

        layer.documents.forEach { doc ->
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

                    sentence.wordforms.forEach { wordform ->
                        val wfElem = xml.createElement("w").apply { setAttribute("xml:id", wordform.id) }
                        wfElem.appendChild(xml.createTextNode(wordform.literal))
                        // add terms
                        sentence.terms.mapValues { it.value.filter { it.targets.any { it.offset == wordform.offset } } }.filterValues { it.isNotEmpty() }
                            .forEach { (type, terms) ->
                                wfElem.setAttribute(type.value, terms.first().value)
                            }

                        sentElem.appendChild(wfElem)
                    }
                }
            }
        }

        val tf = getXmlTransformer()
        tf.transform(DOMSource(root), StreamResult(out))
    }
}