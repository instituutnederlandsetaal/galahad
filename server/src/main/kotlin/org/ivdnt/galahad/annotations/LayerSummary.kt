package org.ivdnt.galahad.annotations

/**
 * Stores the size of the [Layer] in terms of number of [WordForm], [Term], lemma and pos.
 */
data class LayerSummary(
    val annotations: Map<Annotation, Int>,
) {
    constructor(terms: Iterable<Term>) : this(
        annotations = terms.flatMap { it.annotations.keys }.groupingBy { it }
            .eachCount().toSortedMap { a, b -> Annotation.entries.indexOf(a).compareTo(Annotation.entries.indexOf(b)) })

    companion object {
        val EMPTY: LayerSummary = LayerSummary(emptyMap())
    }
}

operator fun LayerSummary.plus(b: LayerSummary): LayerSummary {
    return LayerSummary(
        annotations = this.annotations.toMutableMap().also {
            b.annotations.forEach { (annotation, count) ->
                it.merge(annotation, count, Integer::sum)
            }
        })
}