package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ConverterTest
import org.junit.jupiter.api.Test

class FoliaConverterTest : ConverterTest() {
    @Test
    fun `Folia to Folia`() {
        formatToFormat(DocumentFormat.Folia, DocumentFormat.Folia)
    }

    @Test
    fun `Folia to Tei`() {
        formatToFormat(DocumentFormat.Folia, DocumentFormat.TeiP5)
    }

    @Test
    fun `Folia to Tsv`() {
        formatToFormat(DocumentFormat.Folia, DocumentFormat.Tsv)
    }

    @Test
    fun `Folia to Txt`() {
        formatToFormat(DocumentFormat.Folia, DocumentFormat.Txt)
    }

    @Test
    fun `Folia to Conllu`() {
        formatToFormat(DocumentFormat.Folia, DocumentFormat.Conllu)
    }

    @Test
    fun `Folia to Naf`() {
        formatToFormat(DocumentFormat.Folia, DocumentFormat.Naf)
    }

    @Test
    fun `Folia to Json`() {
        formatToFormat(DocumentFormat.Folia, DocumentFormat.Json)
    }
}