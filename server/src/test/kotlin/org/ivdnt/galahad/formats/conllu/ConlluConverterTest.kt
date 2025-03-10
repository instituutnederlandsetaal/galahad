package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.formats.Resource
import org.ivdnt.galahad.formats.tei.TeiReader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter

class ConlluConverterTest {
    @Test
    fun `Reproduce document`() {
        val reader = TeiReader(Resource.get("tei/docs-pars-sents/input.tei.xml"))
        val layer = reader.layer
        val converter = ConlluConverter(layer)
        val stringWriter = StringWriter()
        val writer = PrintWriter(stringWriter)
        // print to a string
        converter.convert(writer)
        writer.flush()
        val text = stringWriter.toString()
        println(text)
    }
}