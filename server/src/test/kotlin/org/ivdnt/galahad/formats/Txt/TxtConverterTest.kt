package org.ivdnt.galahad.formats.Txt

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ConverterTest
import org.junit.jupiter.api.Test

class TxtConverterTest : ConverterTest() {
    override val folder: String = "pars-sents"
    override val overwriteLayerId: Boolean = true

    @Test
    fun `Txt to Txt`() {
        formatToFormat(DocumentFormat.Txt, DocumentFormat.Txt)
    }

    @Test
    fun `Txt to Tei`() {
        formatToFormat(DocumentFormat.Txt, DocumentFormat.TeiP5)
    }

    @Test
    fun `Txt to Folia`() {
        formatToFormat(DocumentFormat.Txt, DocumentFormat.Folia)
    }

    @Test
    fun `Txt to Tsv`() {
        formatToFormat(DocumentFormat.Txt, DocumentFormat.Tsv)
    }

    @Test
    fun `Txt to Conllu`() {
        formatToFormat(DocumentFormat.Txt, DocumentFormat.Conllu)
    }

    @Test
    fun `Txt to Naf`() {
        formatToFormat(DocumentFormat.Txt, DocumentFormat.Naf)
    }
}