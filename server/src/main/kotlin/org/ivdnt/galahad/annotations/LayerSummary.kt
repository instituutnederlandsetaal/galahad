package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Stores the size of the [Layer] in terms of number of [WordForm], [Term], lemma and pos.
 */
data class LayerSummary(
    @JsonProperty("numTokens") val numWordForms: Int = 0,
)

operator fun LayerSummary.plus(b: LayerSummary): LayerSummary {
    return LayerSummary(
        numWordForms = this.numWordForms + b.numWordForms,
    )
}