package org.ivdnt.galahad.evaluation.comparison

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.WordForm

fun <T> symmetricDifference(
    set1: Set<T>,
    set2: Set<T>,
    equals: (T, T) -> Boolean,
): Set<T> {

    val mset1: MutableSet<T> = HashSet(set1)
    val mset2: MutableSet<T> = HashSet(set2)

    for (v1 in set1) {
        for (v2 in set2) {
            if (equals(v1, v2)) {
                mset1.remove(v1)
                mset2.remove(v2)
            }
        }
    }

    return mset1 union mset2
}

data class TermComparison(
    val hypoTerm: Term, // Hypothesis
    val refTerm: Term, // True reference
) {
    /** Full overlap dependent on the word forms. Overlap of position, not lemma/pos. */
    @get:JsonIgnore
    val fullOverlap: Boolean
        get() = symmetricDifference(hypoTerm.targets.toSet(), refTerm.targets.toSet(),
                                    equals = { wf1: WordForm, wf2: WordForm ->
                WordFormComparison(wf1, wf2).fullOverlap
            }).isEmpty()

    /** Partial overlap dependent on the word forms. Overlap of position, not lemma/pos. */
    // Currently not used.
    @get:JsonIgnore
    val partialOverlap: Boolean
        get() {
            hypoTerm.targets.forEach { target1 ->
                refTerm.targets.forEach { target2 ->
                    if (WordFormComparison(target1, target2).partialOverlap) {
                        return true
                    }
                }
            }
            return false
        }

    /**
     * Apply a removal regex transformation to the annotation before comparing. E.g. removing _ from lemmas.
     */
    fun equalAnnotation(annotation: AnnotationType, regex: Regex): Boolean {
        var refAnnot: String? = refTerm.annotations[annotation]
        var hypAnnot: String? = hypoTerm.annotations[annotation]

        if (refAnnot != null) {
            refAnnot = regex.replace(refAnnot, "")
        }
        if (hypAnnot != null) {
            hypAnnot = regex.replace(hypAnnot, "")
        }

        return equalAnnotation(refAnnot, hypAnnot)
    }

    fun equalAnnotation(annotation: AnnotationType): Boolean {
        val refAnnot: String? = refTerm.annotations[annotation]
        val hypAnnot: String? = hypoTerm.annotations[annotation]
        return equalAnnotation(refAnnot, hypAnnot)
    }

    fun equalAnnotation(ref: String?, hyp: String?): Boolean {
        if (ref == null) return true
        if (ref.isEmpty()) return true
        if (hyp == null) return false
        return hyp.equals(ref, true)
    }

    companion object {
        const val MISSING_MATCH = "Missing match"
    }
}