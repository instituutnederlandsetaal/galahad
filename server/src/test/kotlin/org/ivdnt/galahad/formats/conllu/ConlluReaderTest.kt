package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.formats.Resource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConlluReaderTest {
    @Test
    fun `Documents, paragraphs, sentences`() {
        val reader = ConlluReader(Resource.get("conllu/docs-pars-sents/input.conllu"))
        val layer = reader.layer

        assertEquals(2, layer.documents.size)
        assertEquals(2, layer.documents[0].paragraphs.size)
        assertEquals(2, layer.documents[0].paragraphs[0].sentences.size)
        assertEquals(2, layer.documents[0].paragraphs[1].sentences.size)

        assertEquals(2, layer.documents[1].paragraphs.size)
        assertEquals(2, layer.documents[1].paragraphs[0].sentences.size)
        assertEquals(2, layer.documents[1].paragraphs[1].sentences.size)
    }

    @Test
    fun `Empty nodes`() {
        val reader = ConlluReader(Resource.get("conllu/empty-nodes.conllu"))
        val text = "Sue likes coffee and Bill tea\n" // LF because reader.layer produces a valid unix file.
        assertEquals(text, reader.layer.toString())
    }

    @Test fun `Multi-word tokens`() {
        val reader = ConlluReader(Resource.get("conllu/mw.conllu"))
        val text = "Gas dalla statua .\n"
        assertEquals(text, reader.layer.toString())
    }

}