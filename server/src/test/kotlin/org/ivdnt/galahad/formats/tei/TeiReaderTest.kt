package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.formats.Resource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TeiReaderTest {

    @Test
    fun `Documents, paragraphs, sentences`() {
        val reader = TeiReader(Resource.get("tei/docs-pars-sents/input.tei.xml"))
        val layer = reader.layer
        println(layer)
        assertEquals(2, layer.documents.size)
        assertEquals(2, layer.documents[0].paragraphs.size)
        assertEquals(2, layer.documents[0].paragraphs[0].sentences.size)
        assertEquals(2, layer.documents[0].paragraphs[1].sentences.size)

        assertEquals(2, layer.documents[1].paragraphs.size)
        assertEquals(2, layer.documents[1].paragraphs[0].sentences.size)
        assertEquals(2, layer.documents[1].paragraphs[1].sentences.size)
    }

}