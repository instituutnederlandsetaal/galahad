package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConlluReaderTest {
    @Test
    fun `Empty nodes`() {
        val reader = ConlluReader(TestUtil.get("formats/conllu/empty-nodes.conllu"))
        val text = "Sue likes coffee and Bill tea\n" // LF because reader.layer produces a valid unix file.
        assertEquals(text, reader.layer.toString())
    }

    @Test
    fun `Multi-word tokens`() {
        val reader = ConlluReader(TestUtil.get("formats/conllu/mw.conllu"))
        val text = "Gas dalla statua.\nTer hoogte van.\n" // LF because reader.layer produces a valid unix file.
        assertEquals(text, reader.layer.toString())
    }
}