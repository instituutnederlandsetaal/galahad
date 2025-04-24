package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonIgnore


class Term(
    val id: String,
    val offset: Int,
    val annotations: Annotations,
    spaceAfter: Boolean? = null
) {
    val spaceAfter: Boolean? = if (spaceAfter == false) false else null

    @get:JsonIgnore
    val space: String = if (spaceAfter == false) "" else " "

    @get:JsonIgnore
    val token: String = annotations[Annotation.TOKEN]!!

    @get:JsonIgnore
    val lemma: String? get() = annotations[Annotation.LEMMA]

    @get:JsonIgnore
    val pos: String? = annotations[Annotation.POS]

    @get:JsonIgnore
    val upos: String? = annotations[Annotation.UPOS]

    @get:JsonIgnore
    val head: String? = annotations[Annotation.HEAD]

    @get:JsonIgnore
    val deprel: String? = annotations[Annotation.DEPREL]

    @get:JsonIgnore
    val ner: String? = annotations[Annotation.NER]

    /**
     * Returns a term with the same data, except its offset is aligned to that of [refTerm].
     */
    fun alignedTo(refTerm: Term): Term = Term(id, refTerm.offset, annotations, spaceAfter)

    fun isMulti(annotation: Annotation): Boolean = annotations[annotation]?.contains("+") == true

    /**
     * Returns the annotation head or NO_[annotation] if it is missing.
     * E.g. NOU-C for NOU-c(num=sg); or NO_POS.
     */
    fun annotationHeadOrMissing(annotation: Annotation): String = annotationHead(annotation) ?: missingName(annotation)

    /**
     * Returns the annotation or NO_[annotation] if it is missing.
     * E.g. NOU-C(num=sg); or NO_POS.
     */
    fun annotationOrMissing(annotation: Annotation): String = annotations[annotation] ?: missingName(annotation)

    /**
     * The head of [annotation]. E.g. "PD+NOU" for "PD(type=art)+NOU(num=sg)"
     * or "VG" for "VG|neven" or ORG for B-ORG.
     */
    fun annotationHead(annotationType: Annotation): String? {
        // get annotation
        val annotation = annotations[annotationType] ?: return null
        // for NER
        if (annotationType == Annotation.NER) {
            if ('-' in annotation) {
                return annotation.split('-')[1]
            }
        }
        // for POS & UPOS
        else if (annotationType in posAnnotations) {
            return if (isMulti(annotationType)) {
                // Split on + and transform each part
                annotation.split("+").joinToString("+") { singlePosToHead(it) }
            } else {
                singlePosToHead(annotation)
            }
        }
        // else leave as is
        return annotation
    }

    companion object {
        val EMPTY: Term = Term("", 0, mapOf(Annotation.TOKEN to ""))
        private val posAnnotations: Array<Annotation> = arrayOf(Annotation.POS, Annotation.UPOS)
        private val posHeadSeparators: Array<Char> = arrayOf('(', '|')

        // simply uppercase and prepend "NO_"
        fun missingName(annotation: Annotation): String = "NO_${annotation.value.uppercase()}"

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
            for (separator in posHeadSeparators) {
                if (separator in pos) {
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