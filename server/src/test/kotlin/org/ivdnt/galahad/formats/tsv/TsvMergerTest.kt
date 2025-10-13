package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.MergerTest
import org.junit.jupiter.api.Test

class TsvMergerTest: MergerTest() {
    override val format: DocumentFormat = DocumentFormat.Tsv
    override val folder: String = "tsv/merger"

    @Test
    fun `Merge`() {
        merge()
    }
}