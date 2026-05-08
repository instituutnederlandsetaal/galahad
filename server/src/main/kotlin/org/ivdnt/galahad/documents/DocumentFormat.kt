package org.ivdnt.galahad.documents

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.exceptions.InvalidDocumentFormatException
import org.ivdnt.galahad.util.XmlUtil
import java.io.File
import javax.xml.stream.XMLStreamReader

/** Format of a document. No 1-to-1 correspondence with extensions due to xml format overlap. */
enum class DocumentFormat(val identifier: String, val extension: String) {
    TeiP4Legacy("tei-p4-legacy", "tei.xml"),
    TeiP5("tei-p5", "tei.xml"),
    Naf("naf", "naf.xml"),
    Tsv("tsv", "tsv"),
    Conllu("conllu", "conllu"),
    Folia("folia", "folia.xml"),
    Txt("txt", "txt"),
    Docx("docx", "docx"),
    Pdf("pdf", "pdf"),
    Json("json", "json");

    // Force print identifier only.
    @JsonValue override fun toString(): String = identifier

    companion object {
        val extensions: Set<String>
            get() = entries.map { "." + it.extension }.toSet()

        // Used by Spring.
        @JsonCreator
        fun fromString(s: String): DocumentFormat =
            entries.firstOrNull { it.identifier == s }
                ?: throw InvalidDocumentFormatException(
                    "Invalid format $s, valid formats are $entries"
                )

        /** Get format of a document based on its file extension or content (e.g. root XML node). */
        fun fromFile(file: File): DocumentFormat =
            when (file.extension.lowercase()) {
                "tsv" -> Tsv
                "folia" -> Folia
                "conllu" -> Conllu
                "docx" -> Docx
                "xml",
                "tei" -> determineXmlFormat(file) // TEI can be either P4 or P5, so still check.
                "txt" -> Txt
                "naf" -> Naf
                "pdf" -> Pdf
                "json" -> Json
                else ->
                    throw InvalidDocumentFormatException(
                        "Could not determine document format of ${file.name}."
                    )
            }

        /** Determine xml format based on the root node. */
        private fun determineXmlFormat(file: File): DocumentFormat {
            file.inputStream().use { stream ->
                val sr = XmlUtil.inputFactory.createXMLStreamReader(stream)
                while (sr.hasNext()) {
                    if (sr.next() == XMLStreamReader.START_ELEMENT) {
                        return when (sr.localName.lowercase()) {
                            "folia" -> Folia
                            "tei.2",
                            "teicorpus.2" -> TeiP4Legacy
                            "tei",
                            "teicorpus" -> TeiP5
                            "naf" -> Naf
                            else -> break // and throw
                        }
                    }
                }
            }
            throw InvalidDocumentFormatException(
                "Could not determine document format of ${file.name}."
            )
        }
    }
}
