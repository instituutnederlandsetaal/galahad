package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ConverterTest
import org.junit.jupiter.api.Test

class ConlluConverterTest : ConverterTest() {
    /** Conllu has no root node id */
    override val overwriteLayerId: Boolean = true

    @Test
    fun `Conllu to Conllu`() {
        formatToFormat(DocumentFormat.Conllu, DocumentFormat.Conllu)
    }

    @Test
    fun `Conllu to Tei`() {
        formatToFormat(DocumentFormat.Conllu, DocumentFormat.TeiP5)
    }

    @Test
    fun `Conllu to Folia`() {
        formatToFormat(DocumentFormat.Conllu, DocumentFormat.Folia)
    }

    @Test
    fun `Conllu to Tsv`() {
        formatToFormat(DocumentFormat.Conllu, DocumentFormat.Tsv)
    }

    @Test
    fun `Conllu to Txt`() {
        formatToFormat(DocumentFormat.Conllu, DocumentFormat.Txt)
    }

    @Test
    fun `Conllu to Naf`() {
        formatToFormat(DocumentFormat.Conllu, DocumentFormat.Naf)
    }

    @Test
    fun `Conllu to Json`() {
        formatToFormat(DocumentFormat.Conllu, DocumentFormat.Json)
    }
}
