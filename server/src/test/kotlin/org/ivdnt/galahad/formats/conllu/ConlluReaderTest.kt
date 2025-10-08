package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ReaderTest
import org.junit.jupiter.api.Test

class ConlluReaderTest : ReaderTest() {
    override val format: DocumentFormat = DocumentFormat.Conllu

    @Test
    fun `Empty nodes`() {
        assertLayerAndText("formats/conllu/reader/empty-nodes")
    }

    @Test
    fun `Multi-word tokens`() {
        assertLayerAndText("formats/conllu/reader/mw")
    }

    @Test
    fun `Read underscore in TOKEN`() {
        assertLayerAndText("formats/conllu/reader/underscore")
    }
}