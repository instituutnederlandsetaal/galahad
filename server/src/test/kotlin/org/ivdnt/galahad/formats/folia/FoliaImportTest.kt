package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Test

class FoliaImportTest {

    @Test
    fun `Import doc with correction tags`() {
        val file = FoliaFile(TestUtil.get("formats/folia/reader/corrections/input.folia.xml"))
        TestUtil.assertPlaintextAndSourcelayer("formats/folia/reader/corrections", file)
    }

    @Test
    fun `Import doc with multiple pos & lemma per word, and morphology tags`() {
        val file = FoliaFile(TestUtil.get("formats/folia/reader/morphology/input.folia.xml"))
        TestUtil.assertPlaintextAndSourcelayer("formats/folia/reader/morphology", file)

//        val sourceLayer = foliaFile.layer
//        assertEquals(97, sourceLayer.wordForms.size)
//        assertEquals(97, sourceLayer.terms.size)
//
//        val tsvFile = TsvFile(TestUtil.get("folia/hauraki/pie.tsv"))
//
//        val mergeLayer = tsvFile.mapOnPlainText(foliaFile.plaintext, "mappedLayer")
//        assertEquals(89, mergeLayer.wordForms.size)
//        assertEquals(89, mergeLayer.terms.size)
    }

    @Test
    fun `Import plaintext twined with many style tags`() {
        val file = FoliaFile(TestUtil.get("formats/folia/reader/twine/twine.folia.xml"))
        TestUtil.assertPlainText("formats/folia/reader/twine", file)
//        assertEquals(plaintext, file.plaintext)
//        // Source layer should be empty, there are no source annotations
//        val sourceLayer = file.layer
//        assertEquals(0, sourceLayer.wordForms.size)
//        assertEquals(0, sourceLayer.terms.size)
    }
}