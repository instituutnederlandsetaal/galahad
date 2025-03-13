package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty


/**
 * A term in a [Layer]. A term has a [lemma], a [pos] and refers to one or multiple [WordForm].
 * Referring to multiple [WordForm] is used to represent multi-word terms, although it is currently not used.
 * Lemma and pos can be null.
 */
data class Term(
    @JsonProperty("annotations") val annotations: Annotations,
    @JsonProperty("targets") val targets: MutableList<WordForm>,
) {
    constructor(lemma: String?, pos: String?, targets: MutableList<WordForm>) : this(
        mapOf(AnnotationType.LEMMA to lemma, AnnotationType.POS to pos).filterValues { it != null },
        targets
    )

    @get:JsonIgnore
    val lemma: String? = annotations[AnnotationType.LEMMA]

    @get:JsonIgnore
    val pos: String? = annotations[AnnotationType.POS]

    /** Whether the lemma is not null. */
    @get:JsonIgnore
    val hasLemma: Boolean = lemma != null

    /** Whether the pos is not null. */
    @get:JsonIgnore
    val hasPOS: Boolean = pos != null

    @get:JsonIgnore
    val lemmaOrEmpty: String
        get() = lemma ?: ""

    @get:JsonIgnore
    val posOrEmpty: String
        get() = pos ?: ""

    /** Whether this term refers to multiple [WordForm]. */
    @get:JsonIgnore
    val isMultiTarget: Boolean = targets.size > 1

    fun isMulti(annotation: AnnotationType): Boolean = annotations[annotation]?.contains("+") == true

    /** Offset of the first [WordForm] in [targets].*/
    @get:JsonIgnore
    val firstOffset: Int get() = targets.minOfOrNull { it.offset } ?: -1

    /** String constructed from all the [WordForm] in [targets]. */
    @get:JsonIgnore
    val literals: String
        get() = targets.joinToString(" ") { it.literal }

    /**
     * Returns the annotation head or NO_[annotation] if it is missing.
     * E.g. NOU-C for NOU-c(num=sg); or NO_POS.
     */
    fun annotationHeadOrMissing(annotation: AnnotationType): String =
        annotationHead(annotation) ?: missingName(annotation)

    /**
     * Returns the annotation or NO_[annotation] if it is missing.
     * E.g. NOU-C(num=sg); or NO_POS.
     */
    fun annotationOrMissing(annotation: AnnotationType): String = annotations[annotation] ?: missingName(annotation)

    /**
     * The head of [annotation]. E.g. "PD+NOU" for "PD(type=art)+NOU(num=sg)"
     * or "VG" for "VG|neven" or ORG for B-ORG.
     */
    fun annotationHead(annotationType: AnnotationType): String? {
        // get annotation
        val annotation = annotations[annotationType]
        if (annotation == null) {
            return null
        }
        // for NER
        if (annotationType == AnnotationType.NER) {
            if (annotation.contains('-')) {
                return annotation.split('-')[1]
            }
        }
        // for POS & UPOS
        else if (listOf(AnnotationType.POS, AnnotationType.UPOS).contains(annotationType)) {
            return if (isMulti(annotationType)) {
                // Split on + and transform each part
                annotation.split("+").map { singlePosToHead(it) }.joinToString("+")
            } else {
                singlePosToHead(annotation)
            }
        }
        // else leave as is
        return annotation
    }

    companion object {
        val EMPTY: Term = Term(mapOf(), mutableListOf())

        fun missingName(annotationType: AnnotationType): String =
            // simply uppercase and prepend "NO_"
            "NO_${annotationType.value.uppercase()}"

        /** The features of [pos]. E.g. "num=sg" for "NOU(num=sg)". Does not support multi-pos. */
        fun features(pos: String?): String? {
            if (pos == null) return null
            val featureStart: Int = pos.indexOf('(')
            val featureEnd: Int = pos.indexOf(')')
            return if (featureStart != -1 && featureEnd != -1) {
                return pos.slice(featureStart + 1 until featureEnd)
            } else null
        }

        fun singlePosToHead(pos: String): String {
            val separators = listOf('(', '|')
            for (separator in separators) {
                if (pos.contains(separator)) {
                    val head = pos.split(separator)[0]
                    // presumably head won't be empty, but this way we could
                    // parse something like (VRB) if anyone would ever use that
                    return head.ifEmpty { pos }
                }
            }
            return pos
        }
    }
}

data class Term2(
    val value: String,
    val targets: List<WordForm>,
)