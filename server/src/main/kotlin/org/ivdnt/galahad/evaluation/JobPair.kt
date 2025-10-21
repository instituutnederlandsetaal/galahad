package org.ivdnt.galahad.evaluation

class JobPair(
    val hypothesis: String,
    val reference: String = hypothesis,
) {
    override fun toString(): String = "$hypothesis/$reference"

    companion object {
        fun fromString(pair: String): JobPair = pair.split('/').let { JobPair(it[0], it[1]) }
    }
}