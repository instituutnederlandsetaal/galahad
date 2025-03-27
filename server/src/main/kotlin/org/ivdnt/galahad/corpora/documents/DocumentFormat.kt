package org.ivdnt.galahad.corpora.documents

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.apache.logging.log4j.kotlin.logger
import org.ivdnt.galahad.exceptions.InvalidDocumentFormatException
import org.ivdnt.galahad.util.XmlUtil
import org.w3c.dom.Document
import java.io.File
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
        fun fromFile(file: File): DocumentFormat {
            val format = when (file.extension) {
                "tsv" -> Tsv
                "folia" -> Folia
                "conllu" -> Conllu
                "xml", "tei" -> determineXmlFormat(file) // TEI can be either P4 or P5, so still check.
                "txt" -> Txt
                "naf" -> Naf
                else -> Unknown
            }
            logger.debug { "Induced format $format for file ${file.name}" }
            return format
        }

        /**
         * Differentiate based on the root node.
         */
        private fun determineXmlFormat(file: File): DocumentFormat {
            val xml: Document = XmlUtil.builder.parse(file)
            return when (xml.documentElement.tagName) {
                "FoLiA" -> Folia
                "TEI.2", "teiCorpus.2" -> TeiP4Legacy
                "TEI", "teiCorpus" -> determineTeiP5Format(xml)
                "NAF" -> Naf
                else -> Unknown
            }
        }

        /** Differentiate between TeiP5 and TeiP5Legacy by the presence of pos as an XML attribute.
         * - 1 or more pos are present, it's TeiP5
         * - if no pos are present, but at least one type is present, it's TeiP5Legacy
         * - if no pos or type are present, it's unannotated and we default to TeiP5
         */
        private fun determineTeiP5Format(xmlDoc: Document): DocumentFormat {
            val xPath: XPath = XPathFactory.newInstance().newXPath()
            val numPos = xPath.compile("count(.//w[@pos])").evaluate(xmlDoc, XPathConstants.NUMBER) as Double
            val numTypes = xPath.compile("count(.//w[@type])").evaluate(xmlDoc, XPathConstants.NUMBER) as Double
            if (numTypes == 0.0 || numPos > 0) return TeiP5
            return TeiP5Legacy // No pos but at least one type: assume legacy mode
        }
    }
}