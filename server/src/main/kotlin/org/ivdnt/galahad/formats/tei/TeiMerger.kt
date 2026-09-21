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
import org.ivdnt.galahad.util.children
import org.ivdnt.galahad.util.deepcopy
import org.ivdnt.galahad.util.insertAfter
import org.ivdnt.galahad.util.isNotBlank
import org.w3c.dom.Document
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
        // Transformer without pretty print
        TransformerFactory.newInstance()
            .newTransformer()
            .transform(DOMSource(xml), StreamResult(out))
    }

    private fun parse(node: Node) {
        if (node.isNotBlank) {
            // handle text or mixed text node
            handleText(node as Element)
        } else {
            // Children are guaranteed to be non-text due to above check
            node.children.forEach { child ->
                if (child.localName !in TeiReader.IGNORABLE_TAGS) {
                    if (child.localName in arrayOf("w", "pc")) {
                        parseWord(child as Element)
                    } else {
                        parse(child)
                    }
                }
            }
        }
        return
        // Snapshot children to allow safe DOM mutation during iteration
        node.childNodes.deepcopy().forEach { child ->
            if (child.nodeType == Node.TEXT_NODE) {
                // Split existing texts into <w> tags
                val words =
                    child.nodeValue?.split(whitespace)?.filter { it.isNotBlank() } ?: emptyList()
                if (words.isNotEmpty()) {
                    val ns = (node as? Element)?.namespaceURI
                    for (word in words) {
                        val wEl =
                            if (ns != null) xml.createElementNS(ns, "w") else xml.createElement("w")
                        wEl.textContent = word
                        node.insertBefore(wEl, child)
                        // insert space to avoid formatters thinking
                        // the absence of whitespace is significant
                        val newline = xml.createTextNode(" ")
                        node.insertAfter(newline, wEl)
                        parseWord(wEl)
                    }
                    node.removeChild(child)
                }
            } else if (child.localName !in TeiReader.IGNORABLE_TAGS) {
                if (child.localName in arrayOf("w", "pc")) {
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
        //        if (el.textContent != ref.token) return

        el.setAttribute("xml:id", term.hyp.id)
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
                parent.setAttribute("type", hyp.annotationHead(Annotation.NER))
                return@let
            }
            // Else create
            val wrapper = el.ownerDocument.createElementNS(el.namespaceURI, "name")
            wrapper.setAttribute("type", ner)
            parent.replaceChild(wrapper, el)
            wrapper.appendChild(el)
        }
    }

    private sealed class Token {
        class Whitespace() : Token()

        data class Fragment(val text: String, val chain: List<Element>) : Token()

        class WordTag(val el: Element) : Token()
    }

    private fun flatten(n: Node, chain: List<Element>, out: MutableList<Token>) {
        n.childNodes.deepcopy().forEach { c ->
            when (c.nodeType) {
                Node.TEXT_NODE ->
                    Regex("\\S+|\\s+").findAll(c.nodeValue ?: "").forEach { m ->
                        out +=
                            if (m.value.isBlank()) Token.Whitespace()
                            else Token.Fragment(m.value, chain)
                    }
                Node.ELEMENT_NODE ->
                    (c as Element).let {
                        if (it.localName in arrayOf("w", "pc")) out += Token.WordTag(it)
                        else flatten(it, chain + it, out)
                    }
                else -> {}
            }
        }
    }

    // Rebuild nested elements for a run of Frags that share a chain prefix at `depth`.
    private fun build(xml: Document, frags: List<Token.Fragment>, depth: Int = 0): List<Node> {
        val out = mutableListOf<Node>()
        var i = 0
        while (i < frags.size) {
            val chain = frags[i].chain
            if (depth == chain.size) {
                out += xml.createTextNode(frags[i].text)
                i++
                continue
            }
            val el = chain[depth]
            val j =
                (i until frags.size)
                    .takeWhile { depth < frags[it].chain.size && frags[it].chain[depth] === el }
                    .count() + i
            val clone = el.cloneNode(false) as Element
            build(xml, frags.subList(i, j), depth + 1).forEach { clone.appendChild(it) }
            out += clone
            i = j
        }
        return out
    }

    private fun handleText(node: Element) {
        val xml = node.ownerDocument
        val tokens = mutableListOf<Token>().also { flatten(node, emptyList(), it) }
        val words = tokensFromFragments(tokens)

        while (node.hasChildNodes()) node.removeChild(node.firstChild)
        words.forEachIndexed { idx, word ->
            val wEl =
                if (word.size == 1 && word[0] is Token.WordTag) {
                    (word[0] as Token.WordTag).el.also { parseWord(it) }
                } else {
                    val fresh = node.ownerDocument.createElementNS(node.namespaceURI, "w")
                    // not sure if filter is needed
                    build(xml, word.filterIsInstance<Token.Fragment>()).forEach {
                        fresh.appendChild(it)
                    }
                    parseWord(fresh)
                    fresh
                }
            node.appendChild(wEl)
            if (idx != words.lastIndex) node.appendChild(node.ownerDocument.createTextNode(" "))
        }
    }

    private fun tokensFromFragments(tokens: MutableList<Token>): MutableList<List<Token>> {
        // group into words: consecutive Frags, or a lone Opaque, split on Ws
        val words = mutableListOf<List<Token>>()
        var cur = mutableListOf<Token>()
        for (t in tokens) when (t) {
            is Token.Whitespace ->
                if (cur.isNotEmpty()) {
                    words += cur
                    cur = mutableListOf()
                }
            is Token.WordTag -> {
                if (cur.isNotEmpty()) {
                    words += cur
                    cur = mutableListOf()
                }
                words += listOf(t)
            }
            is Token.Fragment -> cur += t
        }
        if (cur.isNotEmpty()) words += cur
        return words
    }

    companion object {
        private val whitespace = Regex("""\s+""")
    }
}
