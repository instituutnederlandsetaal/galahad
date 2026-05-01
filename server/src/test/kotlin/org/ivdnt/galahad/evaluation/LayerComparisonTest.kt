package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.ConfusionLayerFilter
import org.ivdnt.galahad.evaluation.comparison.HeadGroupTermFilter
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.LayerFilter
import org.ivdnt.galahad.util.LayerBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LayerComparisonTest {

    @Nested
    inner class TermFilter {
        @Test
        fun `Matching layers with pos filter`() {
            val builder =
                LayerBuilder()
                    .loadDummies(100, pos = "NOU")
                    .loadDummies(20, pos = "VRB")
                    .loadDummies(30, pos = "ADJ")
            val hypo = builder.build()
            val ref = builder.build()
            val comparison =
                LayerComparison(hypothesis = hypo, reference = ref, filter = NouNouFilter())
            assertEquals(100, comparison.matches.size)
        }

        @Test
        fun `Layers with partial match`() {
            val hypo = LayerBuilder().loadDummies(100, pos = "NOU").build()
            val ref =
                LayerBuilder()
                    .loadDummies(50, pos = "NOU")
                    .loadDummies(20, pos = "VRB")
                    .loadDummies(30, pos = "ADJ")
                    .build()
            val comparison =
                LayerComparison(hypothesis = hypo, reference = ref, filter = NouVrbFilter())
            assertEquals(20, comparison.matches.size)
        }

        private fun NouNouFilter(): LayerFilter {
            val termFilter = HeadGroupTermFilter(Annotation.POS, "NOU")
            return ConfusionLayerFilter(termFilter, termFilter)
        }

        private fun NouVrbFilter(): LayerFilter =
            ConfusionLayerFilter(
                HeadGroupTermFilter(Annotation.POS, "NOU"),
                HeadGroupTermFilter(Annotation.POS, "VRB"),
            )

        @Test
        fun `Matching layers with pos filter, but different pos`() {
            val hypo = LayerBuilder().loadDummies(100, pos = "NOU").build()
            val ref = LayerBuilder().loadDummies(100, pos = "VRB").build()
            val comparison =
                LayerComparison(hypothesis = hypo, reference = ref, filter = NouNouFilter())
            // The filter will not match any TermComparisons, because none are NOU-NOU.
            assertEquals(0, comparison.matches.size)
        }
    }

    @Nested
    inner class Punctuation {
        @Test
        fun `Matching layers despite punctuation difference`() {
            assertPunctuationDifference("dummy, dummy", 3, 0, 1)
            assertPunctuationDifference("dummy dummy, dummy", 4, 0, 1)
            assertPunctuationDifference("dummy dummy, dummy dummy", 5, 0, 1)
            assertPunctuationDifference("dummy, dummy, dummy,", 6, 0, 3)
        }

        // Because we only fix for one punctuation mark at the end of a word. No more.
        @Test
        fun `Non-matching layers due to punctuation difference`() {
            val hypo =
                LayerBuilder() // dummy " :
                    .loadDummies(1)
                    .loadDummies(1, "\"")
                    .loadDummies(1, ":")
                    .build()
            val ref =
                LayerBuilder() // dummy":
                    .loadDummies(1, "dummy\":")
                    .build()
            val comparison = LayerComparison(hypothesis = hypo, reference = ref)
            assertEquals(3, comparison.matches.size)
            assertEquals(2, comparison.matches.filter { it.ref == Term.EMPTY }.size)
            assertEquals(0, comparison.matches.filter { it.hyp == Term.EMPTY }.size)
        }

        /**
         * Creates a reference layer based on the text, split on spaces. Then creates a hypothesis
         * layer based on the same text, but with punctuation split from the word.
         */
        private fun assertPunctuationDifference(
            text: String,
            numMatches: Int,
            numHypoMissing: Int,
            numRefMissing: Int,
        ) {
            val words = text.split(" ")
            val hypoBuilder = LayerBuilder()
            val refBuilder = LayerBuilder()
            var numPunct = 0
            // Construct
            for (word in words) {
                refBuilder.loadText(word)
                if (!word.last().isLetterOrDigit()) {
                    hypoBuilder.loadText(word.substring(0, word.length - 1))
                    hypoBuilder.loadText(word.last().toString())
                    numPunct++
                } else {
                    hypoBuilder.loadText(word)
                }
            }
            // To layer
            val hypo = hypoBuilder.build()
            assertEquals(words.size + numPunct, hypo.terms.count())
            val ref = refBuilder.build()
            assertEquals(words.size, ref.terms.count())
            // Compare
            val comparison = LayerComparison(hypothesis = hypo, reference = ref)
            assertEquals(numMatches, comparison.matches.size)
            assertEquals(numHypoMissing, comparison.matches.filter { it.hyp == Term.EMPTY }.size)
            assertEquals(numRefMissing, comparison.matches.filter { it.ref == Term.EMPTY }.size)
        }
    }

    // Layers that all have the same terms with the same word forms with the same offsets and
    // length.
    @Nested
    inner class BasicLayersTest {
        private fun assertLayers(
            numHypoTerms: Int,
            numRefTerms: Int,
            numMatches: Int,
            numHypoMissing: Int,
            numRefMissing: Int,
        ) {
            val hypo = LayerBuilder().loadDummies(numHypoTerms).build()
            val ref = LayerBuilder().loadDummies(numRefTerms).build()
            val comparison = LayerComparison(hypothesis = hypo, reference = ref)
            assertEquals(numMatches, comparison.matches.size)
            val hypoNoMatch = comparison.matches.filter { it.hyp == Term.EMPTY }
            val refNoMatch = comparison.matches.filter { it.ref == Term.EMPTY }
            assertEquals(numHypoMissing, hypoNoMatch.size)
            assertEquals(numRefMissing, refNoMatch.size)
        }

        @Test
        fun `Matching layers`() {
            assertLayers(
                numHypoTerms = 100,
                numRefTerms = 100,
                numMatches = 100,
                numHypoMissing = 0,
                numRefMissing = 0,
            )
        }

        @Test
        fun `A layer is 1 term larger than the other`() {
            assertLayers(
                numHypoTerms = 2,
                numRefTerms = 3,
                numMatches = 3,
                numHypoMissing = 1,
                numRefMissing = 0,
            )
            assertLayers(
                numHypoTerms = 100,
                numRefTerms = 101,
                numMatches = 101,
                numHypoMissing = 1,
                numRefMissing = 0,
            )
            assertLayers(
                numHypoTerms = 101,
                numRefTerms = 100,
                numMatches = 101,
                numHypoMissing = 0,
                numRefMissing = 1,
            )
        }

        @Test
        fun `A layer is empty`() {
            assertLayers(
                numHypoTerms = 100,
                numRefTerms = 0,
                numMatches = 100,
                numHypoMissing = 0,
                numRefMissing = 100,
            )
            assertLayers(
                numHypoTerms = 0,
                numRefTerms = 100,
                numMatches = 100,
                numHypoMissing = 100,
                numRefMissing = 0,
            )
        }
    }
}
