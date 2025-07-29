package org.ivdnt.galahad.documents

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class DocumentMetadataTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    @Test
    fun `Properties for an unannotated file`() {
        val path = "formats/shared-converter/input.txt"
        val file = TestUtil.get(path)
        val plaintext = file.readText()
        val doc = TestUtil.getDoc(path)
        val meta = doc.metadata
        assertEquals("input.txt", meta.name)
        assertEquals(DocumentFormat.Txt, meta.format)
        assertEquals(plaintext, meta.text) // This works because the preview is < MAX_PREVIEW_LENGTH
        val total = meta.summary.annotations[Annotation.TOKEN]
        assertEquals(0, total)
    }

    @Test
    fun `Properties for an annotated file`() {
        val path = "all-formats/input/input.tei.xml"
        val doc = TestUtil.getDoc(path)
        val plaintext = TestUtil.getLayer(doc).toString()
        val meta = doc.metadata
        assertEquals("input.tei.xml", meta.name)
        assertEquals(DocumentFormat.TeiP5, meta.format)
        assertEquals(plaintext, meta.text) // This works because the preview is < MAX_PREVIEW_LENGTH
        val total = meta.summary.annotations[Annotation.TOKEN]
        assertEquals(21, total)
    }
}