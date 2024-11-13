package org.ivdnt.galahad.evaluation.comparison

import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.evaluation.confusion.MULTIPLE_POS
import org.ivdnt.galahad.evaluation.confusion.OTHER_POS
import org.ivdnt.galahad.evaluation.confusion.OTHER_POS_REGEX

interface TermFilter {
    fun filter(term: Term): Boolean
}

class CombinedTermFilter(
    val filters: List<TermFilter>
) : TermFilter {
    override fun filter(term: Term): Boolean = filters.all { it.filter(term) }
}

class BasicTermFilter(private val annotationType: AnnotationType, private val value: String): TermFilter {
    override fun filter(term: Term): Boolean = term.annotationOrMissing(annotationType) == value
}

/**
 * A filter for annotation types that have a head, like pos. To be used with [LayerFilter]
 *
 * @param value When equal to the string [MULTIPLE_POS], will look for multi analysis like PD+NOU.
 * When equal to [OTHER_POS], will look for the [OTHER_POS_REGEX] to filter on annotations that don't start with a-z
 * (e.g. Gysseling pos, which is just a number).
 */
class HeadGroupTermFilter(private val annotationType: AnnotationType, private val value: String): TermFilter {
    // Filter methods
    private val multiFilter = { t: Term -> t.isMulti(annotationType) }
    private val otherFilter = { t: Term -> t.annotations[annotationType]?.contains(Regex(OTHER_POS_REGEX)) == true }
    private val singleFilter = { t: Term -> t.annotationHeadOrMissing(annotationType) == value }

    // Decide which filter to use on class initialization
    private val filterFunc: (Term) -> Boolean = when {
        (value.uppercase() == MULTIPLE_POS) -> multiFilter
        (value.uppercase() == OTHER_POS) -> otherFilter
        else -> singleFilter
    }

    override fun filter(term: Term): Boolean = filterFunc(term)
}

