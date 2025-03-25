package org.ivdnt.galahad.data.documents

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.Resource
import org.ivdnt.galahad.formats.createCorpus
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class DocumentMetadataTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Properties for an unannotated file`() {
        val path = "all-formats/input/input.txt"
        val file = Resource.get(path)
        val plaintext = file.readText()
        val doc = Resource.getDoc(path)
        val meta = doc.metadata
        assertEquals("input.txt", meta.name)
        assertEquals(DocumentFormat.Txt, meta.format)
        assertEquals(plaintext.count { it.isLetter() }, meta.numAlphabeticChars)
        assertEquals(plaintext.length, meta.numChars)
        assertEquals(plaintext, meta.preview) // This works because the preview is < MAX_PREVIEW_LENGTH
        val layer = meta.layerSummary
        val total = layer.tokens
        assertEquals(0, total)
    }

    @Test
    fun `Properties for an annotated file`() {
        val path = "all-formats/input/input.tei.xml"
        val file = Resource.get(path)
        val doc = Resource.getDoc(path)
        val plaintext = doc.plaintext
        val meta = doc.metadata
        assertEquals("input.tei.xml", meta.name)
        assertEquals(DocumentFormat.TeiP5, meta.format)
        assertEquals(plaintext.count { it.isLetter() }, meta.numAlphabeticChars)
        assertEquals(plaintext.length, meta.numChars)
        assertEquals(plaintext, meta.preview) // This works because the preview is < MAX_PREVIEW_LENGTH
        val layer = meta.layerSummary
        assertEquals(21, layer.tokens)
    }
}