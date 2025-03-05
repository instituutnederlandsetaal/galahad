package org.ivdnt.galahad.formats.plain

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.formats.DocTestBuilder
import org.ivdnt.galahad.formats.Resource
import org.ivdnt.galahad.formats.createCorpus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PlainFileTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Import plain text`() {
        val file = PlainFile(Resource.get("txt/input.txt"))
        assertEquals("placeholder", file.plaintext)
    }

    @Test
    fun `Merging should throw`() {
        val builder = DocTestBuilder(corpus)
        val file = PlainFile(Resource.get("txt/input.txt"))
        assertThrows(Exception::class.java) {
            file.merge(builder.getDummyTransformMetadata(Layer.EMPTY, DocumentFormat.Txt))
        }
    }
}