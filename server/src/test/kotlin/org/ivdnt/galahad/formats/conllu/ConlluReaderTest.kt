package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.formats.Resource
import org.ivdnt.galahad.formats.txt.TxtReader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConlluReaderTest {
    @Test
    fun `Read txt file`() {
        val reader = ConlluReader(Resource.get("conllu/docs.conllu"))
        val layer = reader.layer
        assertEquals(1, layer.documents.size)
        assertEquals(3, layer.documents[0].paragraphs.size)
    }

}