package org.ivdnt.galahad.evaluation.comparison

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.export.DocumentExport

/** An iterator that saves the current item. */
class TermIterator : Iterator<Term?> {
    var current: Term? = null
        private set
    var chars: Int = 0
        private set

    private val iter: Iterator<Term?>

    constructor(iter: Iterator<Term?>) {
        this.iter = iter
        next()
    }

    override fun hasNext(): Boolean = iter.hasNext()

    override fun next(): Term? {
        // add length of now previous term
        if (current != null) {
            chars += (current as Term).token.length
        }
        current = if (iter.hasNext()) iter.next() else null
        return current
    }
}

/**
 * Match the [Layer.terms] of two layers based on their [WordForm] position (offset and length)
 * When filters are provided, only match [TermComparison] that match the filter (on annotation value).
 * In this way, matches only contain samples that you want to download.
 * (Still, aggregating these matches is up to the (corpus/documents) evaluation classes)
 */
open class LayerComparison(
    private val hypothesis: Layer,
    private val reference: Layer,
    private val filter: LayerFilter? = null,
) {
    constructor(export: DocumentExport) : this(export.layer, export.sourceLayer)

    val matches: MutableList<TermComparison> = ArrayList()
    private val hypoIter: TermIterator = TermIterator(hypothesis.terms.iterator())
    private val refIter: TermIterator = TermIterator(reference.terms.iterator())

    /** Iterate through the terms of both layers simultaneously and compare them. */
    init {
        // While non null, compare
        while (hypoIter.current != null && refIter.current != null) {
            if (hypoIter.chars == refIter.chars) {
                match(TermComparison(hypoIter.current!!, refIter.current!!))
                hypoIter.next()
                refIter.next()
            } else {
                if (refIter.chars < hypoIter.chars) {
                    noMatch(refIter.current!!)
                    refIter.next()
                } else {
                    hypoIter.next()
                }
            }
        }
        // Only one or both are null. So refIter.current can be non-null.
        refIter.current?.let { noMatch(it) }
        // And add any remaining terms
        refIter.forEachRemaining { t -> noMatch(t!!) }
    }

    private fun match(comp: TermComparison) {
        if (filter?.filter(comp) != false) {
            matches.add(comp)
        }
    }

    protected open fun noMatch(t: Term) {
        if (filter?.refTermFilter?.filter(t) != false) {
            matches.add(TermComparison(Term.EMPTY, t))
        }
    }

    companion object {
        // It will mostly be a single punctuation mark, but this is more generic.
        val PUNCT: Regex = Regex("""\W$""")

        private fun truncatedPCMatch(comp: TermComparison): Boolean {
            return truncatedPCMatch(comp.hypoTerm, comp.refTerm)
        }

        private fun truncatedPCMatch(t1: Term, t2: Term): Boolean {
            val a: String = t1.token
            val b: String = t2.token
            return truncatePC(a) == b || truncatePC(b) == a
        }

        /** Truncate any punctuation at the end of the string. */
        fun truncatePC(str: String): String {
            return PUNCT.replace(str, "")
        }
    }
}

class DocumentLayerComparison(
    private val hypothesisLayer: Layer,
    private val referenceLayer: Layer,
    private val layerFilter: LayerFilter? = null,
) : LayerComparison(hypothesisLayer, referenceLayer, layerFilter) {

    override fun noMatch(t: Term) {
        if (layerFilter?.refTermFilter?.filter(t) != false) {
            matches.add(TermComparison(hypoTerm = Term.EMPTY, refTerm = t))
        }
    }

    init {
        // matches.addAll(referenceTermsWithoutMatches.map { TermComparison(hypoTerm = Term.EMPTY, refTerm = it) })
    }
}