package org.ivdnt.galahad.annotations

/**
 * Stores the size of the [Layer] in terms of number of [WordForm], [Term], lemma and pos.
 */
data class LayerSummary(
    val tokens: Int,
)

operator fun LayerSummary.plus(b: LayerSummary): LayerSummary {
    return LayerSummary(
        tokens = this.tokens + b.tokens,
    )
}