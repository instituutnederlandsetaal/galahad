package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.formats.Resource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TxtReaderTest {
    @Test
    fun `Read txt file`() {
        val reader = TxtReader(Resource.get("txt/paragraphs.txt"))
        val layer = reader.layer
        println(layer)
        assertEquals(1, layer.documents.size)
        assertEquals(3, layer.documents[0].paragraphs.size)
    }
}