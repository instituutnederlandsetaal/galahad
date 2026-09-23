package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.MergerTest
import org.junit.jupiter.api.Test

class FoliaMergerTest : MergerTest() {
    override val format: DocumentFormat = DocumentFormat.Folia

    @Test
    fun `Merge`() {
        merge("folia/merger/basic")
    }
}
