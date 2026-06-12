package org.ivdnt.galahad.formats.reader

import javax.xml.XMLConstants
import org.codehaus.stax2.XMLStreamWriter2

class PrettyXMLWriter(private val writer: XMLStreamWriter2) : XMLStreamWriter2 by writer {
    private var indentLevel = 0
    private val indentStr = " ".repeat(4)
    private var newLine = true

    private fun indent() {
        if (newLine) {
            writer.writeCharacters("\n" + indentStr.repeat(indentLevel))
            newLine = false
        }
    }

    override fun writeStartElement(localName: String) {
        indent()
        writer.writeStartElement(localName)
        indentLevel++
        newLine = true
    }

    override fun writeEndElement() {
        indentLevel--
        indent()
        writer.writeEndElement()
        newLine = true
    }

    fun writeEndElement(indent: Boolean = true) {
        indentLevel--
        if (indent) indent()
        writer.writeEndElement()
        newLine = true
    }

    fun writeEmptyElement(localName: String, attrs: Map<String, String>?) {
        indent()
        writer.writeEmptyElement(localName)
        writeAttrs(attrs)
        newLine = true
    }

    override fun writeEmptyElement(localName: String) {
        writeEmptyElement(localName, attrs = null)
    }

    override fun writeCharacters(text: String) {
        writer.writeCharacters(text)
        newLine = false
    }

    fun writeCharacters(text: String, indent: Boolean) {
        if (indent) indent()
        writeCharacters(text)
    }

    fun writeNewLine() {
        newLine = true
    }

    fun wrapIn(localName: String, vararg attrs: Pair<String, String>, block: () -> Unit = {}) {
        writeStartElement(localName)
        val attrsMap = attrs.takeIf { it.isNotEmpty() }?.toMap()
        writeAttrs(attrsMap)
        block()
        writeEndElement()
    }

    fun writeElement(localName: String, attrs: Map<String, String>? = null, text: String? = null) {
        wrapIn(localName) {
            writeAttrs(attrs)
            text?.let { writeCharacters(it) }
        }
    }

    fun writeElement(localName: String, attr: Pair<String, String>? = null, text: String? = null) {
        writeElement(localName, attr?.let { mapOf(it) }, text)
    }

    fun writeElement(localName: String, typeAttr: String? = null, text: String? = null) {
        writeElement(localName, typeAttr?.let { "type" to it }, text)
    }

    fun writeElement(localName: String, text: String? = null) {
        writeElement(
            localName,
            attrs = null, // this argument is mandatory for resolving the correct overload
            text = text,
        )
    }

    fun writeAttrs(attrs: Map<String, String>?) {
        attrs?.forEach { (key, value) ->
            if (key.contains("xml:")) {
                writeAttribute(XMLConstants.XML_NS_URI, key.removePrefix("xml:"), value)
            } else {
                writeAttribute(key, value)
            }
        }
    }
}
