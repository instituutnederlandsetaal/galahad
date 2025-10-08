package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ReaderTest
import org.junit.jupiter.api.Test

class FoliaReaderTest : ReaderTest() {
    override val format: DocumentFormat = DocumentFormat.Folia

    @Test
    fun `Correction tags`() {
        assertLayerAndText("formats/folia/reader/corrections")
    }

    @Test
    fun `Import doc with multiple pos & lemma per word, and morphology tags`() {
        assertLayerAndText("formats/folia/reader/morphology")
    }

    @Test
    fun `Import plaintext twined with many style tags`() {
        assertLayerAndText("formats/folia/reader/twine")
    }
}