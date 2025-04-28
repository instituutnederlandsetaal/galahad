package org.ivdnt.galahad.formats.Tsv

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ConverterTest
import org.junit.jupiter.api.Test

class TsvConverterTest : ConverterTest() {
    override val folder: String = "pars-sents"
    override val overwriteLayerId: Boolean = true

    @Test
    fun `Tsv to Tsv`() {
        formatToFormat(DocumentFormat.Tsv, DocumentFormat.Tsv)
    }

    @Test
    fun `Tsv to Tei`() {
        formatToFormat(DocumentFormat.Tsv, DocumentFormat.TeiP5)
    }

    @Test
    fun `Tsv to Folia`() {
        formatToFormat(DocumentFormat.Tsv, DocumentFormat.Folia)
    }

    @Test
    fun `Tsv to TXT`() {
        formatToFormat(DocumentFormat.Tsv, DocumentFormat.Txt)
    }

    @Test
    fun `Tsv to Conllu`() {
        formatToFormat(DocumentFormat.Tsv, DocumentFormat.Conllu)
    }

    @Test
    fun `Tsv to Naf`() {
        formatToFormat(DocumentFormat.Tsv, DocumentFormat.Naf)
    }
}