package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.MergerTest
import org.junit.jupiter.api.Test

class TeiMergerTest : MergerTest() {
    override val format: DocumentFormat = DocumentFormat.TeiP5

    @Test
    fun `Merge`() {
        merge("tei/merger/basic")
    }

    @Test
    fun `Merge with seg and dictionary definition`() {
        merge("tei/merger/seg")
    }
}
