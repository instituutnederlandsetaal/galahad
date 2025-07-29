package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TermComparisonTest {
    @Nested
    inner class LemmaPosTest {
        // Default values would make the terms equal.
        private fun assertTerm(
            lemmaEqual: Boolean = true, posEqual: Boolean = true,
            hypoLemma: String? = "school", refLemma: String? = "school",
            hypoPos: String? = "NOU", refPos: String? = "NOU",
        ) {
            val hypoTerm = Term("", 0, mapOf(Annotation.LEMMA to hypoLemma, Annotation.POS to hypoPos))
            val refTerm = Term("", 0, mapOf(Annotation.LEMMA to refLemma, Annotation.POS to refPos))
            TermComparison(hypoTerm, refTerm).apply {
                assertEquals(lemmaEqual, equalAnnotation(Annotation.LEMMA))
                assertEquals(posEqual, equalAnnotation(Annotation.POS))
            }
        }

        @Test
        fun `Equal lemma-pos`() {
            assertTerm() // default values
            // That includes being equal as empty or null.
            assertTerm(lemmaEqual = true, hypoLemma = "", refLemma = "")
            assertTerm(lemmaEqual = true, hypoLemma = null, refLemma = null)
            assertTerm(posEqual = true, hypoPos = "", refPos = "")
            assertTerm(posEqual = true, hypoPos = null, refPos = null)
        }

        @Test
        fun `Different lemma-pos`() {
            assertTerm(lemmaEqual = false, hypoLemma = "school", refLemma = "scholen")
            assertTerm(posEqual = false, hypoPos = "NOU", refPos = "ADJ")
        }

        @Test
        fun `Hypothesis lemma-pos is empty or null`() {
            assertTerm(lemmaEqual = false, hypoLemma = "", refLemma = "school")
            assertTerm(lemmaEqual = false, hypoLemma = null, refLemma = "school")
            assertTerm(posEqual = false, hypoPos = "", refPos = "NOU")
            assertTerm(posEqual = false, hypoPos = null, refPos = "NOU")
        }

        // If no reference lemma-pos is defined, any hypothesis is fine.
        // For example, the reference is punctuation with no lemma-pos defined.
        // Some taggers (=hypothesis) do add a lemma-pos. So we'll allow it.
        @Test
        fun `Reference lemma-pos is empty or null`() {
            assertTerm(lemmaEqual = true, hypoLemma = "school", refLemma = "")
            assertTerm(lemmaEqual = true, hypoLemma = "school", refLemma = null)
            assertTerm(posEqual = true, hypoPos = "NOU", refPos = "")
            assertTerm(posEqual = true, hypoPos = "NOU", refPos = null)

            // Note that the hypothesis can even be empty or null. Anything is fine.
            assertTerm(lemmaEqual = true, hypoLemma = "", refLemma = null)
            assertTerm(lemmaEqual = true, hypoLemma = null, refLemma = "")
            assertTerm(posEqual = true, hypoPos = "", refPos = null)
            assertTerm(posEqual = true, hypoPos = null, refPos = "")
        }
    }
}