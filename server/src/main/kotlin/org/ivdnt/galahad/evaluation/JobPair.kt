package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.evaluation.comparison.LayerFilter

class JobPair(
    val hypothesis: String,
    val reference: String = hypothesis,
    val filter: LayerFilter? = null,
) {
    override fun toString(): String = "$hypothesis/$reference"

    companion object {
        fun fromString(pair: String): JobPair = pair.split('/').let { JobPair(it[0], it[1]) }
    }
}
