package org.ivdnt.galahad.formats.xml

import org.codehaus.stax2.XMLStreamWriter2

class PrettyXMLStreamWriter(private val writer: XMLStreamWriter2) : XMLStreamWriter2 by writer {
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

    override fun writeEmptyElement(localName: String) {
        indent()
        writer.writeEmptyElement(localName)
        newLine = true
    }

    override fun writeCharacters(text: String) {
        writer.writeCharacters(text)
        newLine = false
    }
}
