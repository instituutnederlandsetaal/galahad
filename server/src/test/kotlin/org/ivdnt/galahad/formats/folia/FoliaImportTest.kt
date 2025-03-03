package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.formats.Resource
import org.ivdnt.galahad.formats.assertPlaintextAndSourcelayer
import org.ivdnt.galahad.formats.tsv.TSVFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FoliaImportTest {

    @Test
    fun `Import doc with correction tags`() {
        val file = FoliaFile(Resource.get("folia/corrections/input.folia.xml"))
        assertPlaintextAndSourcelayer("folia/corrections", file)
    }

    @Test
    fun `Import doc with multiple pos & lemma per word, and morphology tags`() {
        val foliaFile = FoliaFile(Resource.get("folia/hauraki/input.folia.xml"))
        val expectedPlain = Resource.get("folia/hauraki/plaintext.txt").readText()

        assertEquals(expectedPlain, foliaFile.plainText().readText().trim())

        val sourceLayer = foliaFile.sourceLayer()
        assertEquals(97, sourceLayer.wordForms.size)
        assertEquals(97, sourceLayer.terms.size)

        val tsvFile = TSVFile(Resource.get("folia/hauraki/pie.tsv"))

        val mergeLayer = tsvFile.mapOnPlainText(foliaFile.plainText().readText(), "mappedLayer")
        assertEquals(89, mergeLayer.wordForms.size)
        assertEquals(89, mergeLayer.terms.size)
    }

    @Test
    fun `Import plaintext twined with many style tags`() {
        val file = FoliaFile(Resource.get("folia/twine/twine.folia.xml"))
        val plaintext = Resource.get("folia/twine/plaintext.txt").readText()
        assertEquals(plaintext, file.plainText().readText())
        // Source layer should be empty, there are no source annotations
        val sourceLayer = file.sourceLayer()
        assertEquals(0, sourceLayer.wordForms.size)
        assertEquals(0, sourceLayer.terms.size)
    }
}