package org.ivdnt.galahad.evaluation

class JobPair(
    val reference: String,
    val hypothesis: String? = reference,
) {
    override fun toString(): String = "$reference/$hypothesis"

    companion object {
        fun fromString(pair: String): JobPair = pair.split('/').let {JobPair(it[0], it[1])}
    }
}