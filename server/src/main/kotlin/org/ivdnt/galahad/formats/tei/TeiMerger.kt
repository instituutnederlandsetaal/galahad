package org.ivdnt.galahad.formats.tei

import java.io.OutputStream
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.flatMap
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerMerger
import org.ivdnt.galahad.util.TermIterator
import org.ivdnt.galahad.util.XmlUtil
import org.ivdnt.galahad.util.children
import org.ivdnt.galahad.util.deepcopy
import org.ivdnt.galahad.util.isNotBlank
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

class TeiMerger(export: DocumentExport) : LayerMerger(export) {
    val xml = XmlUtil.builder.parse(export.sourceDocument.sourceFile)
    // If there is no reference term, we won't even be able to find it in the source file.
    var termI = 0
    val termIndices =
        export.layer.documents.flatMap {
            it.paragraphs.flatMap {
                it.sentences.flatMap { it.terms.mapIndexed { index, term -> index + 1 } }
            }
        }
    val termIter: TermIterator = TermIterator(export.layer.terms.iterator())
    var totalChars: Int = 0

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
    }

    private fun parseWord(el: Element) {
        val term = termIter.current!!
        el.setAttribute("xml:id", term.id)
        if (term.pos != null) {
            if (term.upos == null) {
                // just POS
                el.setAttribute("pos", term.pos)
            } else {
                // both
                val pos = "${term.annotationHead(Annotation.UPOS)} ${term.pos}"
                el.setAttribute("pos", pos)
            }
        } else if (term.upos != null) {
            // just UPOS
            el.setAttribute("pos", term.annotationHead(Annotation.UPOS))
        }
        term.lemma?.let { el.setAttribute("lemma", it) }
        term.upos?.let { term.features(Annotation.UPOS)?.let { el.setAttribute("msd", it) } }
        term.deprel?.let {
            el.setAttribute("depR", "${term.head}:$it")
            el.setAttribute("depN", termIndices[termI].toString())
        }
        if (term.spaceAfter == false) {
            el.setAttribute("join", "right")
        } else {
            el.removeAttribute("join")
        }
        term.ner?.let { ner ->
            val parent = el.parentNode
            // Merge with existing
            if (parent is Element && parent.localName == "name") {
                parent.setAttribute("type", term.annotationHead(Annotation.NER))
                return@let
            }
            // Else create
            val wrapper = el.ownerDocument.createElementNS(el.namespaceURI, "name")
            wrapper.setAttribute("type", ner)
            parent.replaceChild(wrapper, el)
            wrapper.appendChild(el)
        }
        // Move termiter.
        val token = textContent(el)
        totalChars += token.count { !it.isWhitespace() }
        while (totalChars >= termIter.chars + termIter.currentCount() && termIter.hasNext()) {
            termIter.next()
            termI++
        }
    }

    // If this <w> contains a <seg>, we readout its text content.
    // Otherwise it is all text nodes, recursive, in non ignored nodes
    private fun textContent(el: Element): String {
        val seg = el.children.firstOrNull { it.localName == "seg" }
        return if (seg != null) {
            seg.textContent
        } else {
            recurseTextContent(el)
        }
    }

    private fun recurseTextContent(el: Element): String {
        return el.children
            .map {
                when (it.nodeType) {
                    Node.TEXT_NODE -> it.nodeValue ?: ""
                    Node.ELEMENT_NODE ->
                        if (it.localName !in TeiReader.IGNORABLE_TAGS)
                            recurseTextContent(it as Element)
                        else ""
                    else -> ""
                }
            }
            .joinToString("")
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
