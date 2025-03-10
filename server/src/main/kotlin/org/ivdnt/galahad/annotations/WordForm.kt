package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A word form is a single word as it appears in the text.
 */
class WordForm(
    /** Literal is not part of the NAF spec, but it greatly speeds up internal processing */
    @JsonProperty val literal: String,
    @JsonProperty val offset: Int,
    @JsonProperty val length: Int,
    @JsonProperty var id: String,
    @JsonProperty val spaceAfter: Boolean = true,
) {
    @get:JsonIgnore
    val endOffset: Int get() = offset + length
}