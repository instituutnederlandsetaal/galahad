package org.ivdnt.galahad.formats.tei

import java.io.OutputStream
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.flatMap
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerMerger
import org.ivdnt.galahad.util.XmlUtil
import org.ivdnt.galahad.util.deepcopy
import org.w3c.dom.Element
import org.w3c.dom.Node

class TeiMerger(export: DocumentExport) : LayerMerger(export) {
    val xml = XmlUtil.builder.parse(export.sourceDocument.sourceFile)
    val termIter = termComparisons.iterator()
    var termI = -1
    val termIndices =
        export.layer.documents.flatMap {
            it.paragraphs.flatMap {
                it.sentences.flatMap { it.terms.mapIndexed { index, term -> index + 1 } }
            }
        }

    override fun merge(out: OutputStream) {
        parse(xml.documentElement as Node)
        TransformerFactory.newInstance()
            .newTransformer()
            .transform(DOMSource(xml), StreamResult(out))
    }

    private fun parse(node: Node) {
        // Snapshot children to allow safe DOM mutation during iteration
        node.childNodes.deepcopy().forEach { child ->
            if (child.nodeType == Node.TEXT_NODE) {
                val words =
                    child.nodeValue?.split(whitespace)?.filter { it.isNotBlank() } ?: emptyList()
                if (words.isNotEmpty()) {
                    val ns = (node as? Element)?.namespaceURI
                    for (word in words) {
                        val wEl =
                            if (ns != null) xml.createElementNS(ns, "w") else xml.createElement("w")
                        wEl.textContent = word
                        node.insertBefore(wEl, child)
                        parseWord(wEl)
                    }
                    node.removeChild(child)
                }
            } else if (child.localName !in TeiReader.IGNORABLE_TAGS) {
                if (child.localName in TeiReader.WORD_TAGS) {
                    parseWord(child as Element)
                } else {
                    parse(child)
                }
            }
        }
    }

    private fun parseWord(el: Element) {
        val term = termIter.next()
        termI += 1
        val hyp = term.hyp
        val ref = term.ref
        if (el.textContent != ref.token) return

        // pos is either just Annotation.POS, just Annotation.UPOS, or space separated "head(UPOS)
        // POS".

        if (hyp.pos != null) {
            if (hyp.upos == null) {
                // just POS
                el.setAttribute("pos", hyp.pos)
            } else {
                // both
                val pos = "${hyp.annotationHead(Annotation.UPOS)} ${hyp.pos}"
                el.setAttribute("pos", pos)
            }
        } else if (hyp.upos != null) {
            // just UPOS
            el.setAttribute("pos", hyp.annotationHead(Annotation.UPOS))
        }

        el.setAttribute("xml:id", term.hyp.id)
        hyp.lemma?.let { el.setAttribute("lemma", it) }
        hyp.upos?.let { hyp.features(Annotation.UPOS)?.let { el.setAttribute("msd", it) } }
        hyp.deprel?.let {
            el.setAttribute("depR", "${hyp.head}:$it")
            el.setAttribute("depN", termIndices[termI].toString())
        }
        if (hyp.spaceAfter == false) {
            el.setAttribute("join", "right")
        } else {
            el.removeAttribute("join")
        }

        hyp.ner?.let { ner ->
            val parent = el.parentNode
            // Merge with existing
            if (parent is Element && parent.localName == "name") {
                parent.setAttribute("type", ner)
                return@let
            }

            // Else create
            val wrapper = el.ownerDocument.createElementNS(el.namespaceURI, "name")
            wrapper.setAttribute("type", ner)
            parent.replaceChild(wrapper, el)
            wrapper.appendChild(el)
        }
    }

    companion object {
        private val whitespace = Regex("""\s+""")
    }
}
