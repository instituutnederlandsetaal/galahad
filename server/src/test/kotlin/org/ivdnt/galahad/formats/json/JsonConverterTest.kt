package org.ivdnt.galahad.formats.json

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ConverterTest
import org.junit.jupiter.api.Test

class JsonConverterTest : ConverterTest() {
    @Test
    fun `Json to Folia`() {
        formatToFormat(DocumentFormat.Json, DocumentFormat.Folia)
    }

    @Test
    fun `Json to Tei`() {
        formatToFormat(DocumentFormat.Json, DocumentFormat.TeiP5)
    }

    @Test
    fun `Json to Tsv`() {
        formatToFormat(DocumentFormat.Json, DocumentFormat.Tsv)
    }

    @Test
    fun `Json to Txt`() {
        formatToFormat(DocumentFormat.Json, DocumentFormat.Txt)
    }

    @Test
    fun `Json to Conllu`() {
        formatToFormat(DocumentFormat.Json, DocumentFormat.Conllu)
    }

    @Test
    fun `Json to Naf`() {
        formatToFormat(DocumentFormat.Json, DocumentFormat.Naf)
    }
}
