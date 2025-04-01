package org.ivdnt.galahad.util

import com.fasterxml.aalto.stax.InputFactoryImpl
import com.fasterxml.aalto.stax.OutputFactoryImpl
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory

abstract class XmlUtil(
    val xml: Document,
) {
    protected fun Node.getOrCreateChild(childTag: String, prepend: Boolean = false): Element {
        val child: Node? = this.childOrNull(childTag)
        if (child != null) return child as Element
        // No node found
        return createChild(childTag, prepend)
    }

    protected fun Node.createChild(name: String, prepend: Boolean = false): Element {
        val newNode = xml.createElement(name)
        if (prepend && this.childNodes.length > 0) { // TODO checking length might not be necessary
            this.insertBefore(newNode, this.firstChild)
        } else {
            this.appendChild(newNode)
        }
        return newNode
    }

    /**
     * Add a tag to [this] with [name], [textContent], and optional [attrValue]
     * Defaults to writing attribute @type.
     */
    protected fun Node.createChild(
        name: String,
        textContent: String,
        attrValue: String,
    ): Element = this.createChild(name, mapOf("type" to attrValue), textContent)

    protected fun Node.createChild(
        name: String,
        textContent: String,
    ): Element = this.createChild(name, mapOf(), textContent)

    protected fun Node.createChild(
        name: String,
        attr: Pair<String, String>,
        textContent: String = "",
    ): Element = this.createChild(name, mapOf(attr), textContent)

    protected fun Node.createChild(
        name: String,
        attrs: Map<String, String>,
        textContent: String = "",
    ): Element {
        // Create empty tag
        return xml.createElement(name)
            // Add to parent
            .also { appendChild(it) }
            // Set attributes
            .apply { attrs.forEach { (key, value) -> this.setAttribute(key, value) } }
            // Set text content
            .apply { this.textContent = textContent }
    }

    companion object {
        val builder: DocumentBuilder = DocumentBuilderFactory.newInstance().apply {
            isIgnoringComments = true
            isNamespaceAware = true
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        }.newDocumentBuilder()

        val transformer: Transformer = TransformerFactory.newInstance().newTransformer().apply {
            // Pretty print
            setOutputProperty(OutputKeys.INDENT, "yes")
            // For some reason needed to print the root on a new line instead of on the same line as the doctype.
            setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes")
        }
        val inputFactory: XMLInputFactory = InputFactoryImpl().apply {
            setProperty(XMLInputFactory.SUPPORT_DTD, false)
        }

        val outputFactory: XMLOutputFactory = OutputFactoryImpl().apply {
            configureForSpeed()
        }
    }
}