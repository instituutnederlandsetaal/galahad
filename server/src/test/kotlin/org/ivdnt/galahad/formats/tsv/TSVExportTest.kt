package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.util.DocTest
import org.ivdnt.galahad.util.LayerBuilder
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TSVExportTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    @Test
    fun `Convert dummy layer to TSV`() {
        val layer: Layer = LayerBuilder().loadDummies(2).build()
        DocTest.builder( corpus )
            .expectingFile("tsv/export/converted-output.tsv")
            .convertToTSV(layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }

    @Test
    fun `Merge dummy layer with TSV with extra columns`() {
        val layer: Layer = LayerBuilder().loadDummies(2).build()
        DocTest.builder( corpus )
            .expectingFile("tsv/export/merged-output.tsv")
            .mergeTSV("tsv/export/input.tsv", layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }
}