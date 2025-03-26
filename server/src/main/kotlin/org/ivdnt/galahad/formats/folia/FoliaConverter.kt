package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import org.ivdnt.galahad.util.getXmlBuilder
import org.ivdnt.galahad.util.getXmlTransformer
import java.io.OutputStream
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class FoliaConverter(
    export: DocumentExport,
) : LayerConverter(export) {

    override fun convert(out: OutputStream) {
        val xml = getXmlBuilder().newDocument()
        val root = xml.createElement("FoLiA").apply {
            setAttribute("xmlns", "http://ilk.uvt.nl/folia")
            setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink")
            setAttribute("xml:id", export.document.metadata.uuid.toString())
            setAttribute("generator", "galahad.ivdnt.org")
            setAttribute("version", "2.5.3")
        }
        xml.appendChild(root)
        FoliaMetadata(xml, root, export)
        export.layer.documents.forEach { document ->
            val text = xml.createElement("text").apply { setAttribute("id", document.id) }
            root.appendChild(text)
            document.paragraphs.forEach { paragraph ->
                val p = xml.createElement("p").apply { setAttribute("id", paragraph.id) }
                text.appendChild(p)
                paragraph.sentences.forEach { sentence ->
                    val s = xml.createElement("s").apply { setAttribute("id", sentence.id) }
                    p.appendChild(s)
                    sentence.terms.forEach { term ->
                        val w = xml.createElement("w").apply { setAttribute("id", term.id) }
                        s.appendChild(w)
                        val t = xml.createElement("t").apply { textContent = term.token }
                        w.appendChild(t)
                        term.lemma?.let {
                            xml.createElement("lemma").apply {
                                setAttribute("class", it)
                                setAttribute("set", export.tagger.id)
                                setAttribute("processor", export.tagger.id)
                            }
                        }.also { w.appendChild(it) }
                        term.pos?.let {
                            xml.createElement("pos").apply {
                                setAttribute("class", it)
                                setAttribute("head", term.annotationHead(Annotation.POS) ?: "")
                                setAttribute("set", export.tagger.id)
                                setAttribute("processor", export.tagger.id)
                            }
                        }.also { w.appendChild(it) }
                    }
                }
            }
        }
        getXmlTransformer().transform(DOMSource(xml), StreamResult(out))
    }
}