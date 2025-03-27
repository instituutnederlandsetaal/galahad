package org.ivdnt.galahad.annotations

/**
 * The minimum length in chars of a [LayerPreview].
 * If the limit ends halfway a word, we incorporate the entire word.
 */
const val LAYER_PREVIEW_LENGTH: Int = 15

/**
 * A preview of a [Layer] in terms of the first N [WordForm] and [Term], where N is in chars and ruled by [LAYER_PREVIEW_LENGTH].
 */
data class LayerPreview(
    val terms: List<Term>,
) {
    companion object {
        val EMPTY: LayerPreview = LayerPreview(emptyList())
    }
}
