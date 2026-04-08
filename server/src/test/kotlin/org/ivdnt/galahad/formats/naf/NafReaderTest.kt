package org.ivdnt.galahad.formats.naf

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ReaderTest
import org.junit.jupiter.api.Test

class NafReaderTest : ReaderTest() {
    override val format: DocumentFormat = DocumentFormat.Naf

    @Test
    fun `Reader test`() {
        assertLayerAndText("formats/naf/reader")
    }
}