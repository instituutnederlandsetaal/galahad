package org.ivdnt.galahad.annotations

class TermSpan(
    val indices: IntArray,
    val value: String,
) {
    constructor(indices: List<Int>, value: String) : this(indices.toIntArray(), value)
}