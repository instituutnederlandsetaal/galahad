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
    val preview: String,
    /** A truncated preview of the annotated layer. */
    val layerPreview: LayerPreview,
    /** Some statistics about the source annotations, if present */
    val layerSummary: LayerSummary,
    /** Last modified timestamp in milliseconds. */
    val lastModified: Long,
    /** Annotation types in the source layer. */
    val annotations: Set<Annotation>,
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
                preview = text.take(PREVIEW_LENGTH) + if (text.length > PREVIEW_LENGTH) "..." else "",
                layerPreview = file.layer.preview,
                layerSummary = file.layer.summary,
                lastModified = System.currentTimeMillis(),
                annotations = file.layer.terms.flatMap { it.annotations.keys }.toSet()
            )
        }
    }
}