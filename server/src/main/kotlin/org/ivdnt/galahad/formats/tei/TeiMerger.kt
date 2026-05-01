package org.ivdnt.galahad.formats.tei

import java.io.OutputStream
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerMerger
import org.ivdnt.galahad.util.XmlUtil
import org.ivdnt.galahad.util.children
import org.w3c.dom.Element
import org.w3c.dom.Node

class TeiMerger(export: DocumentExport) : LayerMerger(export) {
    val xml = XmlUtil.builder.parse(export.document.sourceFile)
    val termIter = termComparisons.iterator()

    override fun merge(out: OutputStream) {
        parse(xml.documentElement as Node)
        TransformerFactory.newInstance()
            .newTransformer()
            .transform(DOMSource(xml), StreamResult(out))
    }

    private fun parse(node: Node) {
        node.children.forEach {
            if (it.localName !in TeiReader.IGNORABLE_TAGS) {
                if (it.localName in TeiReader.WORD_TAGS) {
                    parseWord(it as Element)
                } else {
                    parse(it)
                }
            }
        }
    }

    private fun parseWord(el: Element) {
        val term = termIter.next()
        val hyp = term.hyp
        val ref = term.ref
        if (el.textContent != ref.token) return

        el.setAttribute("xml:id", term.hyp.id)
        hyp.lemma?.let { el.setAttribute("lemma", it) }
        hyp.pos?.let { el.setAttribute("pos", it) }
        if (hyp.spaceAfter == false) {
            el.setAttribute("join", "right")
        } else {
            el.removeAttribute("join")
        }
    }
}
