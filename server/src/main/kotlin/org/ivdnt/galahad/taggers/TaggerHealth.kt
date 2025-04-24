package org.ivdnt.galahad.taggers

class TaggerHealth(
    val status: TaggerHealthStatus = TaggerHealthStatus.UNKNOWN,
    val queueSizeAtTagger: Int = 0, // bytes
    val processingSpeed: Int = 0, // 'chars/s'
    val message: String = "",
)

enum class TaggerHealthStatus {
    ERROR,
    HEALTHY,
    NOT_HEALTHY,
    UNKNOWN
}