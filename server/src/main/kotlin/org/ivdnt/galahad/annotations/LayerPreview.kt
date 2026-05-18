package org.ivdnt.galahad.annotations

import org.ivdnt.galahad.annotations.LayerPreview.Companion.LAYER_PREVIEW_LENGTH

/** Preview of a [LAYER_PREVIEW_LENGTH] [Term]s of [Layer]. */
data class LayerPreview(val terms: List<Term>) {
    companion object {
        const val LAYER_PREVIEW_LENGTH: Int = 15
        val EMPTY: LayerPreview = LayerPreview(emptyList())
    }
}
