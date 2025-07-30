package org.ivdnt.galahad.evaluation.distribution

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.evaluation.EvaluationUtil
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CorpusDistributionTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    // @Test
    // fun `Distribution of two docs sum up`() {
    //     EvaluationUtil.add_two_docs_to_corpus(corpus)
    //     EvaluationUtil.addDocWithMissingMatches(corpus)
    //     val dist = JobDistribution(corpus, SOURCE_LAYER_NAME, Annotation.POS)
    //     assertEquals(6, dist.distribution.size)
    //     // chars
    //     // csv
    //     assertEquals(TestUtil.get("evaluation/distribution/output.csv").readText(), dist.toCSV())
    // }
}