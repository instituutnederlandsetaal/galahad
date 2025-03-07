package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.formats.DocTest
import org.ivdnt.galahad.formats.LayerBuilder
import org.ivdnt.galahad.formats.createCorpus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConlluExportTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Convert dummy layer to TSV`() {
        val layer: Layer = LayerBuilder().loadDummies(3).build()
        // We'll add some extra edge cases to the dummy layer.
        layer.terms[0] = Term(null, null, mutableListOf(layer.wordForms[0]))
        layer.terms[1] = Term("dummy", "pos(a=1,b=2)", mutableListOf(layer.wordForms[1]))

        DocTest.builder( corpus )
            .expectingFile("conllu/comments/converted-output.conllu")
            .convertToConllu(layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }

    @Test
    fun `Merge dummy layer with Conllu`() {
        val layer: Layer = LayerBuilder().loadDummies(20).build()
        DocTest.builder(corpus)
            .expectingFile("conllu/comments/merged-output.conllu")
            .mergeConllu("conllu/comments/input.conllu", layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }
}