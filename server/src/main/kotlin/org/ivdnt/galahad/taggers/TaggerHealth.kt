package org.ivdnt.galahad.taggers

import com.fasterxml.jackson.annotation.JsonProperty

class TaggerHealth(
    @JsonProperty val status: TaggerHealthStatus = TaggerHealthStatus.UNKNOWN,
    @JsonProperty val queueSizeAtTagger: Int = 0, // bytes
    @JsonProperty val processingSpeed: Int = 0, // 'chars/s'
    @JsonProperty val message: String = "",
)

enum class TaggerHealthStatus {
    ERROR,
    HEALTHY,
    NOT_HEALTHY,
    UNKNOWN
}