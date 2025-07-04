package org.ivdnt.galahad.taggers

class TaggerHealth(
    val status: TaggerHealthStatus = TaggerHealthStatus.UNKNOWN,
    val message: String = "",
)

enum class TaggerHealthStatus {
    ERROR,
    HEALTHY,
    NOT_HEALTHY,
    UNKNOWN
}