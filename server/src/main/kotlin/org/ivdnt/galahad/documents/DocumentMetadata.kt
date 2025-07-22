package org.ivdnt.galahad.documents

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.LayerPreview
import org.ivdnt.galahad.annotations.LayerSummary
import org.ivdnt.galahad.formats.InternalFile

data class DocumentMetadata(
    /** Name of the uploaded file including extension. Used as a working directory name. */
    val name: String,
    /** Format of the uploaded file as induced by FormatInducer. */
    val format: DocumentFormat,
    /** Number of chars in the parsed plaintext. */
    val numChars: Int,
    /** Number of alphabetic chars in the parsed plaintext. */
    val numAlphabeticChars: Int,
    /** A truncated preview of the parsed plaintext. */
    val text: String,
    /** A truncated preview of the annotated layer. */
    val preview: LayerPreview,
    /** Some statistics about the source annotations, if present */
    val summary: LayerSummary,
    /** Last modified timestamp in milliseconds. */
    val modified: Long,
    /** Annotation types in the source layer. */
    val annotations: List<Annotation>,
) {
    companion object {
        private const val PREVIEW_LENGTH: Int = 100

        fun create(file: InternalFile): DocumentMetadata {
            val text = file.layer.toString()
            return DocumentMetadata(
                name = file.file.name,
                format = file.format,
                numChars = text.length,
                numAlphabeticChars = text.filter { it.isLetter() }.length,
                text = text.take(PREVIEW_LENGTH) + if (text.length > PREVIEW_LENGTH) "..." else "",
                preview = file.layer.preview,
                summary = file.layer.summary,
                modified = System.currentTimeMillis(),
                annotations = Annotation.order(file.layer.terms.flatMap { it.annotations.keys }.toList())
            )
        }
    }
}