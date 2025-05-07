package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.util.DocTest
import org.ivdnt.galahad.util.LayerBuilder
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach

internal class FoliaExportTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    @Test
    fun `Convert dummy layer to folia`() {
        val layer = LayerBuilder().loadDummies(3).build()
        DocTest.builder(corpus)
            .expectingFile("folia/export/converted-output.folia.xml")
            // document.parse() will be called and throw on an empty file, hence the dummy file.
            .convertToFolia(TestUtil.get("folia/dummy.folia.xml"), layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }

    @Test
    fun `Merge dummy layer with file containing correction tags`() {
        val layer = LayerBuilder().loadDummies(10, literal="word0 ").build()
        DocTest.builder(corpus)
            .expectingFile("folia/corrections/merged-output.folia.xml")
            .mergeFolia(TestUtil.get("folia/corrections/input.folia.xml"), layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }

    @Test
    fun `Merge pie-tdn result with heavily style tag twined folia`() {
        val plaintext: String = TestUtil.get("folia/twine/plaintext.txt").readText()
        val layer = LayerBuilder()
            .loadLayerFromTSV("folia/twine/pie-tdn.tsv", plaintext)
            .build()
        DocTest.builder(corpus)
            .expectingFile("folia/twine/merged-output.folia.xml")
            .mergeFolia(TestUtil.get("folia/twine/twine.folia.xml"), layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }
}