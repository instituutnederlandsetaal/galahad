package org.ivdnt.galahad.data.layer

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/** Avoid empty strings in the CSV representation. */
fun Term.toNonEmptyPair(): Pair<String, String> {
    return (this.pos ?: Term.NO_POS) to (this.lemma ?: Term.NO_LEMMA)
}


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
        mapOf(AnnotationType.LEMMA to lemma, AnnotationType.POS to pos),
        targets)

    @get:JsonIgnore val lemma: String? = annotations[AnnotationType.LEMMA]
    @get:JsonIgnore val pos: String? = annotations[AnnotationType.POS]

    /** Whether the lemma is not null. */
    @get:JsonIgnore
    val hasLemma: Boolean = lemma != null

    /** Whether the pos is not null. */
    @get:JsonIgnore
    val hasPOS: Boolean = pos != null

    @get:JsonIgnore
    val posHeadGroupOrDefault
        get() = posHeadGroup ?: NO_POS

    @get:JsonIgnore
    val lemmaOrDefault
        get() = lemma ?: NO_LEMMA

    @get:JsonIgnore
    val lemmaOrEmpty
        get() = lemma ?: ""

    @get:JsonIgnore
    val posOrEmpty
        get() = pos ?: ""

    /** Whether this term refers to multiple [WordForm]. */
    @get:JsonIgnore
    val isMultiTarget = targets.size > 1

    /** The head of the first [pos]. E.g. "PD" for "PD(type=art)+NOU(num=sg)". */
    @get:JsonIgnore
    val posHead: String? = annotationToHead(pos)

    @get:JsonIgnore
    val isMultiPos: Boolean = pos?.contains("+") ?: false

    fun isMulti(annotation: AnnotationType): Boolean {
        return annotations[annotation]?.contains("+") ?: false
    }

    /** The head of all [pos]. E.g. "PD+NOU" for "PD(type=art)+NOU(num=sg)". */
    @get:JsonIgnore
    val posHeadGroup: String? = run {
        // Split on +
        if (!isMultiPos) return@run posHead
        val result: String? = pos?.split("+")?.mapNotNull { annotationToHead(it) }?.joinToString("+")
        result
    }

    @get:JsonIgnore
    val posHeadGroupOrEmpty
        get() = posHeadGroup ?: ""

    /** Offset of the first [WordForm] in [targets].*/
    @get:JsonIgnore
    val firstOffset get() = targets.minOfOrNull { it.offset } ?: -1

    /** String constructed from all the [WordForm] in [targets]. */
    @get:JsonIgnore
    val literals: String
        get() = targets.joinToString(" ") { it.literal }

    fun annotationToGroupHeadOrDefault(annotation: AnnotationType): String {
        val annot = annotations[annotation] ?: return missingName(annotation)
        if (!annot.contains("+")) return annotationToHead(annot)!!
        return annot.split("+").mapNotNull { annotationToHead(it) }.joinToString("+")
    }

    fun annotationOrMissing(annotation: AnnotationType): String {
        return annotations[annotation] ?: missingName(annotation)
    }

    companion object {
        const val NO_POS = "NO_POS"
        const val NO_LEMMA = "NO_LEMMA"
        val EMPTY = Term(mapOf(), mutableListOf())

        fun missingName(annotationType: AnnotationType): String {
            // simply uppercase and prepend "NO_"
            return "NO_${annotationType.value.uppercase()}"
        }

        /**
         * The head of [annotation]. E.g. "PD" for "PD(type=art)" or "VG" for "VG|neven".
         * But does not support groups like "ADP()+VRB()"
         */
        fun annotationToHead(annotation: String?): String? {
            if (annotation == null) {
                return null
            }

            val headSeparators = listOf('(', '|')
            for (separator in headSeparators) {
                if (annotation.contains(separator)) {
                    val head = annotation.split(separator)[0]
                    return head.ifEmpty { annotation }
                }
            }
            // for NER
            if (annotation.contains('-')) {
                return annotation.split('-')[1]
            }

            // pos is 0 or more letters only
            return annotation
        }

        /** The features of [pos]. E.g. "num=sg" for "NOU(num=sg)". Does not support multi-pos. */
        fun features(pos: String?): String? {
            if (pos == null) return null
            val featureStart: Int = pos.indexOf('(')
            val featureEnd: Int = pos.indexOf(')')
            return if (featureStart != -1 && featureEnd != -1) {
                return pos.slice(featureStart + 1 until featureEnd)
            } else null
        }
    }
}