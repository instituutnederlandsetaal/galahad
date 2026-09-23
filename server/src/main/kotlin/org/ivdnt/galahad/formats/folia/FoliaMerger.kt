package org.ivdnt.galahad.formats.folia

import java.io.OutputStream
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.flatMap
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerMerger
import org.ivdnt.galahad.formats.folia.FoliaWriter.Companion.alphaNumeric
import org.ivdnt.galahad.util.TermIterator
import org.ivdnt.galahad.util.XmlUtil
import org.ivdnt.galahad.util.children
import org.ivdnt.galahad.util.deepcopy
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

class FoliaMerger(export: DocumentExport) : LayerMerger(export) {
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
        if (node.localName == "t") {
            // handle text or mixed text node
            handleText(node as Element)
        } else {
            // Children are guaranteed to be non-text due to above check
            node.childNodes.deepcopy().forEach { child ->
                if (child.localName !in FoliaReader.IGNORABLE_TAGS) {
                    if (child.localName == "w") {
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
        term.lemma?.let {
            val lemma = xml.createElement("lemma")
            lemma.setAttribute("class", it)
            lemma.setAttribute("processor", export.layers.name)
            el.appendChild(lemma)
        }
        term.pos?.let {
            val pos = xml.createElement("pos")
            pos.setAttribute("class", it)
            pos.setAttribute("processor", export.layers.name)
            el.appendChild(pos)
        }
        term.upos?.let {
            val upos = xml.createElement("upos")
            upos.setAttribute("class", it)
            upos.setAttribute("processor", export.layers.name)
            upos.setAttribute("set", "ud")
            el.appendChild(upos)
        }
        if (term.pos == "PC" && !term.token.contains(FoliaWriter.alphaNumeric)) {
            el.setAttribute("class", "PUNCTUATION")
        } else {
            el.setAttribute("class", "WORD")
        }
        if (term.spaceAfter == false) {
            el.setAttribute("space", "no")
        } else {
            el.removeAttribute("space")
        }
        // Move termiter.
        val token = textContent(el)
        totalChars += token.count { !it.isWhitespace() }
        while (totalChars >= termIter.chars + termIter.currentCount() && termIter.hasNext()) {
            termIter.next()
            termI++
        }
    }

    // Each <w> contains a <t>, we readout its text content.
    private fun textContent(el: Element): String {
        val t = el.children.firstOrNull { it.localName == "t" }
        return recurseTextContent(t as Element)
    }

    private fun recurseTextContent(el: Element): String {
        return el.children
            .map {
                when (it.nodeType) {
                    Node.TEXT_NODE -> it.nodeValue ?: ""
                    Node.ELEMENT_NODE ->
                        if (it.localName !in FoliaReader.IGNORABLE_TAGS)
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
                        if (it.localName == "w") out += Token.WordTag(it)
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
                    val text = node.ownerDocument.createElementNS(node.namespaceURI, "t")
                    fresh.appendChild(text)
                    // not sure if filter is needed
                    build(xml, word.filterIsInstance<Token.Fragment>()).forEach {
                        text.appendChild(it)
                    }
                    parseWord(fresh)
                    fresh
                }
            node.parentNode.insertBefore(wEl, node)
            if (idx != words.lastIndex)
                node.parentNode.insertBefore(node.ownerDocument.createTextNode(" "), node)
        }
        // Remove the original node after processing
        node.parentNode.removeChild(node)
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
