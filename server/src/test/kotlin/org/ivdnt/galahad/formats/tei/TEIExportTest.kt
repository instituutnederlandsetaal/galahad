package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.formats.*
import org.ivdnt.galahad.taggers.Tagset
import org.ivdnt.galahad.util.DocTest
import org.ivdnt.galahad.util.LayerBuilder
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TEIExportTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    @Test
    fun `Merge pie-tdn result with heavily twined tei`() {
        val file = TeiFile(TestUtil.get("tei/twine/twine.input.xml"))
        TestUtil.assertPlainText("tei/twine", file)

        val plaintext: String = TestUtil.get("tei/twine/plaintext.txt").readText()
        val layer = LayerBuilder().loadLayerFromTSV("tei/twine/pie-tdn.tsv", plaintext).build()
        DocTest.builder(corpus).expectingFile("tei/twine/merged-output.xml")
            .mergeTEI(TestUtil.get("tei/twine/twine.input.xml"), layer).ignoreTrailingWhiteSpaces().ignoreDate()
            .ignoreUUID().ignoreWhiteSpaceDocumentWide().result()
    }

    @Test
    fun `Merge doc with alphanumeric PC`() {
        fun asserAlphaNumericPC(folder: String) {
            val file = TeiFile(TestUtil.get("$folder/input.tei.xml"))
            TestUtil.assertPlainText(folder, file)

            val plaintext: String = TestUtil.get("$folder/plaintext.txt").readText()
            val tagset = Tagset.readOrThrow("TDN-Core")
            val layer = LayerBuilder().loadLayerFromTSV("$folder/pie-tdn.tsv", plaintext).build()

            DocTest.builder(corpus).expectingFile("$folder/merged-output.xml")
                .mergeTEI(TestUtil.get("$folder/input.tei.xml"), layer).ignoreDate().ignoreUUID().result()
        }
        asserAlphaNumericPC("tei/alphanumericpc/with-w-tags")
        asserAlphaNumericPC("tei/alphanumericpc/without-w-tags")
    }

    @Test
    fun `Convert doc with alphanumeric PC`() {
        val folder = "tei/alphanumericpc/with-w-tags"
        val plaintext: String = TestUtil.get("$folder/plaintext.txt").readText()
        val tagset = Tagset.readOrThrow("TDN-Core")
        val layer = LayerBuilder().loadLayerFromTSV("$folder/pie-tdn.tsv", plaintext).build()
        DocTest.builder(corpus).expectingFile("$folder/converted-output.xml")
            .convertToTEI(TestUtil.get("$folder/input.tei.xml"), layer).ignoreDate().ignoreUUID().result()
    }

    @Test
    fun `Merge a pie-tdn layer with a tei file that only contains plaintext`() {
        val file = TeiFile(TestUtil.get("tei/brieven/input.tei.xml"))
        TestUtil.assertPlainText("tei/brieven", file)

        val plaintext: String = TestUtil.get("tei/brieven/plaintext.txt").readText()
        val layer = LayerBuilder().loadLayerFromTSV("tei/brieven/pie.tsv", plaintext).build()
        DocTest.builder(corpus).expectingFile("tei/brieven/merged-output.tei.xml")
            .mergeTEI(TestUtil.get("tei/brieven/input.tei.xml"), layer).ignoreDate().ignoreUUID()
            .ignoreWhiteSpaceDocumentWide().result()
    }

    @Test
    fun punctuationExportTest() {

        val teiFile = TeiFile(TestUtil.get("tei/oneparagraph/mocktei.xml"))
        DocTest.builder(corpus).expecting("Dit is wat oefentekst.").got(teiFile.layer.toString())
            .ignoreTrailingWhiteSpaces().result()

        val tagset = Tagset.readOrThrow("TDN-Core")

        val layer = LayerBuilder().loadLayerFromTSV(
            "tei/export/mock-TDN-with-punctuation.tsv", teiFile.layer.toString()
        ).build()

        DocTest.builder(corpus).expectingFile("tei/export/mock-TDN-with-punctuation-result.xml")
            .convertToTEI(teiFile.file, layer).ignoreDate().ignoreUUID()
            // When using just .ignoreWhiteSpace() the test fails, even though comparison tools shows no difference
            .ignoreWhiteSpaceDocumentWide().result()
    }

    @Test
    fun mergePuncutationTest() {

        val tagset = Tagset.readOrThrow("TDN-Core")
        val plaintext = TeiFile(TestUtil.get("tei/dummies/punctutation-mixed-tags.xml")).layer.toString()
        val layer = LayerBuilder().loadLayerFromTSV("tei/dummies/punctuation-mixed-tags-sample-layer.tsv", plaintext)
            .build()

        DocTest.builder(corpus).expectingFile("tei/export/punctuation-mixed-tags-merge-export-result.xml")
            .mergeTEI("tei/dummies/punctutation-mixed-tags.xml", layer).ignoreDate().ignoreUUID().result()
    }
}