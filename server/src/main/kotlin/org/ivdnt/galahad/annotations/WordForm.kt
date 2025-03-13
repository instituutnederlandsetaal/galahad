package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A wordform is a single word as it appears in the text.
 */
data class WordForm(
    @JsonProperty val literal: String,
    @JsonProperty val offset: Int,
    @JsonProperty var id: String,
    @JsonProperty val spaceAfter: Boolean = true,
) {
    @JsonIgnore
    val endOffset: Int = offset + literal.length

    override fun toString(): String = "[$literal]"
}