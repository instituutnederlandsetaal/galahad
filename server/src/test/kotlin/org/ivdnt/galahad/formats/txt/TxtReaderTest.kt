package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.formats.ReaderTest
import org.ivdnt.galahad.formats.tsv.TsvFile
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TxtReaderTest : ReaderTest() {
    override val format: DocumentFormat = DocumentFormat.Txt

    @Test
    fun `Ignore trailing whitespace before and after sentence`() {
        assertLayerAndText("formats/txt/reader/whitespace")
    }
}