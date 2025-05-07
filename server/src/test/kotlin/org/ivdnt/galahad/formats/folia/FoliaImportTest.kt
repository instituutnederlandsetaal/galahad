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
    }

    @Test
    fun `Import plaintext twined with many style tags`() {
        val file = FoliaFile(TestUtil.get("formats/folia/reader/twine/twine.folia.xml"))
        TestUtil.assertPlainText("formats/folia/reader/twine", file)
    }
}