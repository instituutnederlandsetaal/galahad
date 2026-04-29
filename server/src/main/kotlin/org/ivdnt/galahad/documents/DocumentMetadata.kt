package org.ivdnt.galahad.documents

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.LayerAnnotations
import org.ivdnt.galahad.annotations.LayerPreview
import org.ivdnt.galahad.formats.InternalFile

data class DocumentMetadata(
    /** Name of the uploaded file including extension. Used as a working directory name. */
    val name: String,
    /** Format of the uploaded file as induced by FormatInducer. */
    val format: DocumentFormat,
    /** A truncated preview of the parsed plaintext. */
    val text: String,
    /** A truncated preview of the annotated layer. */
    val preview: LayerPreview,
    /** Some statistics about the source annotations, if present */
    val annotations: LayerAnnotations,
    /** Last modified timestamp in milliseconds. */
    val modified: Long,
) {
    @get:JsonIgnore
    val annotationSet: List<Annotation>
        get() = annotations.annotations.map { it.key }

    companion object {
        private const val PREVIEW_LENGTH: Int = 100

        fun create(file: InternalFile): DocumentMetadata {
            val text = file.layer.toString()
            return DocumentMetadata(
                name = file.file.name,
                format = file.format,
                text = text.take(PREVIEW_LENGTH) + if (text.length > PREVIEW_LENGTH) "..." else "",
                preview = file.layer.preview,
                annotations = file.layer.summary,
                modified = System.currentTimeMillis(),
            )
        }
    }
}