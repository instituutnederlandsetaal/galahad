package org.ivdnt.galahad.data.document

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.formats.InternalFile
import java.util.*

class DocumentMetadata private constructor(
    /** Name of the uploaded file including extension. Used as a working directory name. */
    @JsonProperty("name") val name: String,
    /** Format of the uploaded file as induced by FormatInducer. */
    @JsonProperty("format") val format: DocumentFormat,
    /** Number of chars in the parsed plaintext. */
    @JsonProperty("numChars") val numChars: Int,
    /** Number of alphabetic chars in the parsed plaintext. */
    @JsonProperty("numAlphabeticChars") val numAlphabeticChars: Int,
    /** A truncated preview of the parsed plaintext. */
    @JsonProperty("preview") val preview: String,
    /** A truncated preview of the annotated layer. */
    @JsonProperty("layerPreview") val layerPreview: LayerPreview,
    /** Some statistics about the source annotations, if present */
    @JsonProperty("layerSummary") val layerSummary: LayerSummary,
    /** Last modified timestamp in milliseconds. */
    @JsonProperty("lastModified") val lastModified: Long,
    /** UUID of the document. */
    @JsonProperty("uuid") val uuid: UUID,
    /** Annotation types in the source layer. */
    @JsonProperty("annotationTypes") val annotationTypes: Set<String> = setOf(),
) {
    companion object {
        const val PREVIEW_LENGTH: Int = 100

        fun create(internalFile: InternalFile): DocumentMetadata {
            // expensive
            val annotationTypes =
                internalFile.sourceLayer.terms.flatMap { it.annotations.keys }.map { it.value }.toMutableSet()

            val text = internalFile.plaintext
            return DocumentMetadata(
                name = internalFile.file.name,
                format = internalFile.format,
                numChars = text.length,
                numAlphabeticChars = text.filter { it.isLetter() }.length,
                preview = text.take(PREVIEW_LENGTH) + if (text.length > PREVIEW_LENGTH) "..." else "",
                layerPreview = internalFile.sourceLayer.preview,
                layerSummary = internalFile.sourceLayer.summary,
                lastModified = System.currentTimeMillis(),
                uuid = UUID.randomUUID(),
                annotationTypes = annotationTypes
            )
        }
    }
}