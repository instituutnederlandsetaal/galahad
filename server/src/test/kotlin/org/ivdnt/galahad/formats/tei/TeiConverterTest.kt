package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ConverterTest
import org.junit.jupiter.api.Test

class TeiConverterTest : ConverterTest() {
    @Test
    fun `Tei to Tei`() {
        formatToFormat(DocumentFormat.TeiP5, DocumentFormat.TeiP5)
    }

    @Test
    fun `Tei to Folia`() {
        formatToFormat(DocumentFormat.TeiP5, DocumentFormat.Folia)
    }

    @Test
    fun `Tei to Tsv`() {
        formatToFormat(DocumentFormat.TeiP5, DocumentFormat.Tsv)
    }

    @Test
    fun `Tei to TXT`() {
        formatToFormat(DocumentFormat.TeiP5, DocumentFormat.Txt)
    }

    @Test
    fun `Tei to Conllu`() {
        formatToFormat(DocumentFormat.TeiP5, DocumentFormat.Conllu)
    }

    @Test
    fun `Tei to Naf`() {
        formatToFormat(DocumentFormat.TeiP5, DocumentFormat.Naf)
    }
}