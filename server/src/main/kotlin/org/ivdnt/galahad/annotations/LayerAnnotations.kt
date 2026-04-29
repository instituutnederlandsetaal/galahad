package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Stores the size of the [Layer] in terms of number of [WordForm], [Term], lemma and pos.
 */
data class LayerAnnotations(
    @JsonValue
    val annotations: Map<Annotation, Int>,
) {
    companion object {
        val EMPTY: LayerAnnotations = LayerAnnotations(emptyMap<Annotation, Int>())
        fun fromTerms(terms: Iterable<Term>) = LayerAnnotations(
            annotations = terms.flatMap { it.annotations.keys }.groupingBy { it }
                .eachCount().toSortedMap { a, b -> Annotation.entries.indexOf(a).compareTo(Annotation.entries.indexOf(b)) })
    }
}

operator fun LayerAnnotations.plus(b: LayerAnnotations): LayerAnnotations {
    return LayerAnnotations(
        annotations = this.annotations.toMutableMap().also {
            b.annotations.forEach { (annotation, count) ->
                it.merge(annotation, count, Integer::sum)
            }
        })
}
