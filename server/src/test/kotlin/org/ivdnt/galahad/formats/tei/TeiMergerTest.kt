package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.MergerTest
import org.junit.jupiter.api.Test

class TeiMergerTest : MergerTest() {
    override val format: DocumentFormat = DocumentFormat.TeiP5
    override val folder: String = "tei/merger"

    @Test
    fun `Merge`() {
        merge()
    }
}
