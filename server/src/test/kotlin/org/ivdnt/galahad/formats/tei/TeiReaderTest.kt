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

    @Test
    fun `Kranten17`() {
        println(TeiReader(Resource.get("tei/Kranten17/export_1651.xml")).layer)
    }

    @Test
    fun `twine`() {
        println(TeiReader(Resource.get("tei/twine/twine.input.xml")).layer)
    }

    @Test
    fun `peerle`() {
        println(TeiReader(Resource.get("tei/GTBPrototype/151_16_anoniem_peerle_key.xml")).layer)
    }

    @Test
    fun `missiven`() {
        println(TeiReader(Resource.get("tei/MissivenDeel6/INT_5fbd7e2a-99da-33d0-98b8-45e7bc088d46.xml")).layer)
    }
}