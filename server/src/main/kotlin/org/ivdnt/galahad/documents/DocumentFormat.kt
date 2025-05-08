package org.ivdnt.galahad.documents

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.apache.logging.log4j.kotlin.logger
import org.ivdnt.galahad.exceptions.InvalidDocumentFormatException
import org.ivdnt.galahad.util.XmlUtil
import org.w3c.dom.Document
import java.io.File
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * The DocumentFormat will be used to declare the format for indexing in BlackLab and
 * is used to switch between the different parsers to extract plaintext and merge layers.
 */
enum class DocumentFormat(val identifier: String, val extension: String) {
    TeiP4Legacy("tei-p4-legacy", "xml"),
    TeiP5Legacy("tei-p5-legacy", "xml"),
    TeiP5("tei-p5", "tei.xml"),
    Naf("naf", "naf.xml"),
    Tsv("tsv", "tsv"),
    Conllu("conllu", "conllu"),
    Folia("folia", "folia.xml"),
    Txt("txt", "txt"),
    Docx("docx", "docx"),
    Pdf("pdf", "pdf"),
    Unknown("unknown", "unknown");

    @JsonValue
    override fun toString(): String = identifier

    companion object {
        /**
         * BlackLab uses formats that contain -, which is not allowed in an enum, so we need this mapping
         */
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun fromString(s: String): DocumentFormat =
            entries.firstOrNull { it.identifier == s } ?: throw InvalidDocumentFormatException(
                "Invalid format $s, valid formats are ${entries.map { it.identifier }}"
            )

        /**
         * Induce the format of a document based on its file extension and content (e.g. root XML node).
         */
        fun fromFile(file: File): DocumentFormat = when (file.extension) {
            "tsv" -> Tsv
            "folia" -> Folia
            "conllu" -> Conllu
            "docx" -> Docx
            "xml", "tei" -> determineXmlFormat(file) // TEI can be either P4 or P5, so still check.
            "txt" -> Txt
            "naf" -> Naf
            "pdf" -> Pdf
            else -> Unknown
        }

        /**
         * Differentiate based on the root node.
         */
        private fun determineXmlFormat(file: File): DocumentFormat {
            val sr = XmlUtil.inputFactory.createXMLStreamReader(file.inputStream())
            while (sr.hasNext()) {
                if (sr.next() == XMLStreamReader.START_ELEMENT) {
                    return when (sr.localName.lowercase()) {
                        "folia" -> Folia
                        "tei.2", "teicorpus.2" -> TeiP4Legacy
                        "tei", "teicorpus" -> TeiP5
                        "naf" -> Naf
                        else -> Unknown
                    }
                }
            }
            return Unknown // No root element found
        }
    }
}