package org.ivdnt.galahad.data.layer

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.tagset.Tagset

/** A small preview of a [Layer] and some metadata. */
data class LayerMetadata(
    @JsonProperty("preview") val preview: LayerPreview,
    @JsonProperty("name") val name: String,
    @JsonProperty("tagset") val tagset: Tagset,
    @JsonProperty("summary") val summary: LayerSummary,
) {
    companion object {
        fun create(layer: Layer): LayerMetadata = LayerMetadata(
            preview = layer.preview,
            name = layer.name,
            tagset = layer.tagset,
            summary = layer.summary
        )
    }
}