package org.ivdnt.galahad.evaluation.comparison

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.export.DocumentExport

fun Iterator<Term>.nextOrNull(): Term? = if (hasNext()) next() else null

// Some hardcoded punctuation
val PUNCTUATION: List<String> = listOf(",", ".", "?", "!", ":", ";", ")", "(", "'", "\"")

/**
 * Match the [Layer.terms] of two layers based on their [WordForm] position (offset and length)
 * When filters are provided, only match [TermComparison] that match the filter (on annotation value).
 * In this way, matches only contain samples that you want to download.
 * (Still, aggregating these matches is up to the (corpus/documents) evaluation classes)
 */
open class LayerComparison(
    private val hypothesisLayer: Layer,
    private val referenceLayer: Layer,
    private val layerFilter: LayerFilter? = null,
) {
    constructor(export: DocumentExport) : this(export.layer, export.sourceLayer)

    @JsonIgnore
    val matches: MutableList<TermComparison> = ArrayList()

    @JsonIgnore
    val referenceTermsWithoutMatches: MutableList<Term> = ArrayList()

    @JsonIgnore
    val hypothesisTermsWithoutMatches: MutableList<Term> = ArrayList()

    @JsonIgnore
    private val hypoIter: Iterator<Term> = hypothesisLayer.terms.iterator()

    @JsonIgnore
    private val refIter: Iterator<Term> = referenceLayer.terms.iterator()

    @JsonIgnore
    private var currentHypoTerm: Term? = Term.EMPTY

    @JsonIgnore
    private var currentRefTerm: Term? = Term.EMPTY

    init {
        if (refIter.hasNext() && hypoIter.hasNext()) {
            compare()
        } else {
            hypothesisTermsWithoutMatches.addAll(hypothesisLayer.terms)
            referenceTermsWithoutMatches.addAll(referenceLayer.terms)
        }
    }

    /** Iterate through the terms of both layers simultaneously and compare them. */
    private fun compare() {
        // First terms
        nextHypo()
        nextRef()
        // While there are next terms
        while (currentHypoTerm != null && currentRefTerm != null) {
            val comp = TermComparison(hypoTerm = currentHypoTerm!!, refTerm = currentRefTerm!!)
            compareTerm(comp)
        }
        // One of the two could be non-null. These are not included in the remaining refIter.
        currentHypoTerm?.let(::hypoNoMatch)
        currentRefTerm?.let(::refNoMatch)
        // The remaining terms have no matches
        hypoIter.forEachRemaining(::hypoNoMatch)
        refIter.forEachRemaining(::refNoMatch)
    }

    private fun compareTerm(comp: TermComparison) {
        // Act on the comparison
        if (comp.overlap) {
            fullMatch(comp)
        } else {
            // Unequal first offset
            if (comp.hypoTerm.offset < comp.refTerm.offset) {
                hypoNoMatch()
            } else if (comp.hypoTerm.offset > comp.refTerm.offset) {
                refNoMatch()
            }
            // Equal first offset but no match.
            // Try to truncate either terms to see if the last char is punctuation.
            else if (symmetricTruncatedPcMatch(comp)) {
                // If so, still match it.
                fullMatch(comp)
            } else {
                hypoNoMatch()
                refNoMatch()
            }
        }
    }

    private fun fullMatch(comp: TermComparison) {
        if (layerFilter?.filter(comp) != false) {
            matches.add(comp)
        }
        nextHypo()
        nextRef()
    }

    private fun hypoNoMatch() {
        hypoNoMatch(currentHypoTerm!!)
        nextHypo()
    }

    protected open fun hypoNoMatch(t: Term) {
        // Note how layerFilter can be null, and both null and true != false.
        if (layerFilter?.hypoTermFilter?.filter(t) != false) {
            hypothesisTermsWithoutMatches.add(t)
        }
    }

    private fun refNoMatch() {
        refNoMatch(currentRefTerm!!)
        nextRef()
    }

    protected open fun refNoMatch(t: Term) {
        if (layerFilter?.refTermFilter?.filter(t) != false) {
            referenceTermsWithoutMatches.add(t)
        }
    }

    private fun nextHypo() {
        currentHypoTerm = hypoIter.nextOrNull()
    }

    private fun nextRef() {
        currentRefTerm = refIter.nextOrNull()
    }

    companion object {
        fun symmetricTruncatedPcMatch(comp: TermComparison): Boolean {
            val aStr: String = comp.hypoTerm.token
            val bStr: String = comp.refTerm.token
            return truncatedPcMatch(aStr, bStr) || truncatedPcMatch(bStr, aStr)
        }

        fun truncatedPcMatch(aStr: String, bStr: String): Boolean {
            if (aStr.isEmpty() || bStr.isEmpty()) {
                return false
            }
            return truncatePC(aStr) == bStr
        }

        fun truncatePC(str: String): String {
            return if (str.isNotEmpty() && PUNCTUATION.contains(str.last().toString())) {
                str.slice(0 until str.lastIndex)
            } else {
                str
            }
        }
    }
}

class DocumentLayerComparison(
    private val hypothesisLayer: Layer,
    private val referenceLayer: Layer,
    private val layerFilter: LayerFilter? = null,
) : LayerComparison(hypothesisLayer, referenceLayer, layerFilter) {

    override fun hypoNoMatch(t: Term) {
        // ignore
    }

    override fun refNoMatch(t: Term) {
        if (layerFilter?.refTermFilter?.filter(t) != false) {
            matches.add(TermComparison(hypoTerm = Term.EMPTY, refTerm = t))
        }
    }

    init {
        matches.addAll(referenceTermsWithoutMatches.map { TermComparison(hypoTerm = Term.EMPTY, refTerm = it) })
    }
}