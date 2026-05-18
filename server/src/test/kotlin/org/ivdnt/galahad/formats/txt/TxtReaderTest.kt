package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ReaderTest
import org.junit.jupiter.api.Test

class TxtReaderTest : ReaderTest() {
    override val format: DocumentFormat = DocumentFormat.Txt

    @Test
    fun `Ignore trailing whitespace before and after sentence`() {
        assertLayerAndText("formats/txt/reader/whitespace")
    }
}
