package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.MergerTest
import org.junit.jupiter.api.Test

class ConlluMergerTest : MergerTest() {
    override val format: DocumentFormat = DocumentFormat.Conllu
    override val folder: String = "conllu/merger"

    @Test
    fun `Merge`() {
        merge()
    }
}
