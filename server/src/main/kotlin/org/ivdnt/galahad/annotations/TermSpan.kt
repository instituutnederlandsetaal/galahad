package org.ivdnt.galahad.annotations

/** Defines an annotation value spanning multiple [Term]s in [SentenceLayer.terms]. */
class TermSpan(
    /** Indices of the [SentenceLayer.terms] that this span covers. */
    val indices: IntArray,
    /** Annotation value, e.g. a named entity label. */
    val value: String,
) {
    constructor(indices: List<Int>, value: String) : this(indices.toIntArray(), value)
}