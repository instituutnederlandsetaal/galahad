package org.ivdnt.galahad.evaluation.comparison

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term

data class TermComparison(
    val hypoTerm: Term, // Hypothesis
    val refTerm: Term, // True reference
) {
    /**
     * Apply a removal regex transformation to the annotation before comparing. E.g. removing _ from lemmas.
     */
    fun equalAnnotation(annotation: Annotation, regex: Regex): Boolean {
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

    fun equalAnnotation(annotation: Annotation): Boolean {
        val refAnnot: String? = refTerm.annotations[annotation]
        val hypAnnot: String? = hypoTerm.annotations[annotation]
        return equalAnnotation(refAnnot, hypAnnot)
    }

    private fun equalAnnotation(ref: String?, hyp: String?): Boolean {
        if (ref == null) return true
        if (ref.isEmpty()) return true
        if (hyp == null) return false
        return hyp.equals(ref, true)
    }

    companion object {
        const val MISSING_MATCH: String = "Missing match"
    }
}