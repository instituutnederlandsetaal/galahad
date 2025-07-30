package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.formats.xml.PrettyXMLWriter
import org.ivdnt.galahad.util.ifNullOrBlank
import org.ivdnt.galahad.util.withoutFormatExt
import java.text.SimpleDateFormat

class FoliaMetadataWriter(val writer: PrettyXMLWriter, val export: DocumentExport) {
    val title = export.document.uploadedFile.withoutFormatExt
    val pid = export.layer.id
    val corpusName = export.corpus.mutableMetadata.name
    val sourceName = export.corpus.mutableMetadata.sourceName.ifNullOrBlank { "!No source name defined!" }
    val sourceURL = export.corpus.mutableMetadata.sourceURL?.toString().ifNullOrBlank { "!No source URL defined!" }
    val eraFrom = export.corpus.mutableMetadata.eraFrom.toString()
    val eraTo = export.corpus.mutableMetadata.eraTo.toString()
    val language = export.corpus.mutableMetadata.language.ifNullOrBlank { "!No language defined!" }
    val langCode = export.corpus.mutableMetadata.langCode
    val today = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
    val annotations = export.layer.annotations
    val taggerName = export.tagger.id

    fun write() {
        writer.wrapIn("metadata", mapOf("type" to "native")) {
            writeAnnotations()
            writeProvenance()
            writeMeta()
        }
    }

    private fun writeAnnotations() {
        writer.wrapIn("annotations") {
            writer.writeEmptyElement("text-annotation")
            writer.writeEmptyElement("paragraph-annotation")
            writer.writeEmptyElement("sentence-annotation")
            writer.writeEmptyElement("token-annotation")
            if (Annotation.LEMMA in annotations) writeAnnotation("lemma")
            if (Annotation.POS in annotations || Annotation.UPOS in annotations) writeAnnotation("pos")
            if (Annotation.NER in annotations) writeAnnotation("entity")
            if (Annotation.DEPREL in annotations) writeAnnotation("dependency")
        }
    }

    private fun writeAnnotation(annotation: String) {
        writer.wrapIn("$annotation-annotation", mapOf("set" to taggerName)) {
            writer.writeEmptyElement("annotator", mapOf("processor" to taggerName))
        }
    }

    private fun writeProvenance() {
        writer.wrapIn("provenance") {
            writer.writeEmptyElement("processor", mapOf(
                "xml:id" to taggerName,
                "name" to taggerName,
                "type" to "auto",
                "src" to "https://github.com/instituutnederlandsetaal/galahad-taggers-dockerized",
                "host" to "https://galahad.ivdnt.org",
                "user" to export.user.id,
            ))
        }
    }

    private fun writeMeta() {
        writer.writeElement("meta", "id" to "title", title)
        writer.writeElement("meta", "id" to "language", language)
    }
}