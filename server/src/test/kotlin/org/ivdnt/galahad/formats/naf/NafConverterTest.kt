package org.ivdnt.galahad.formats.naf

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ConverterTest
import org.junit.jupiter.api.Test

class NafConverterTest : ConverterTest() {
    override val folder: String = "naf/converter"

    @Test
    fun `Naf to Tei`() {
        formatToFormat(DocumentFormat.Naf, DocumentFormat.TeiP5)
    }

    @Test
    fun `Naf to Folia`() {
        formatToFormat(DocumentFormat.Naf, DocumentFormat.Folia)
    }

    @Test
    fun `Naf to Tsv`() {
        formatToFormat(DocumentFormat.Naf, DocumentFormat.Tsv)
    }

    @Test
    fun `Naf to Txt`() {
        formatToFormat(DocumentFormat.Naf, DocumentFormat.Txt)
    }

    @Test
    fun `Naf to Conllu`() {
        formatToFormat(DocumentFormat.Naf, DocumentFormat.Conllu)
    }

    @Test
    fun `Naf to Naf`() {
        formatToFormat(DocumentFormat.Naf, DocumentFormat.Naf)
    }

    @Test
    fun `Naf to Json`() {
        formatToFormat(DocumentFormat.Naf, DocumentFormat.Json)
    }
}
