package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.formats.ReaderTest
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TsvReaderTest : ReaderTest() {
    override val format: DocumentFormat = DocumentFormat.Tsv

    @Test
    fun `Ignore comments`() {
        assertLayerAndText("formats/tsv/reader/comments")
    }

    @Test
    fun `Extra columns`() {
        assertLayerAndText("formats/tsv/reader/extra-columns")
    }

    @Test
    fun `Missing token column header`() {
        val file = TsvFile(TestUtil.get("formats/tsv/reader/missing-token/missing-token.tsv"))
        assertThrows<DocumentInvalidException> { file.layer }
    }
}