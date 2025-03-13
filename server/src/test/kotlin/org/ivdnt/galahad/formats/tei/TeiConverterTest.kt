package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.formats.Resource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.StringWriter

class TeiConverterTest {
    fun test(path: String) {
        val layer = TeiReader(Resource.get(path)).layer
        val outputStream = ByteArrayOutputStream()
        TeiConverter(layer).convert(outputStream)
        println(outputStream.toString())
    }

    @Test
    fun `Read and write`() {
        test("tei/docs-pars-sents/input.tei.xml")
    }

    @Test
    fun `Rijmbijbel`() {
        test("tei/4000.tei.xml")
    }
}