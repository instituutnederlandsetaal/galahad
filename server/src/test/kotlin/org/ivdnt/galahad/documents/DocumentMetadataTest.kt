package org.ivdnt.galahad.documents

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
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
        val path = "formats/shared/converter/karel_en_martijn.txt"
        val doc = TestUtil.getDoc(path)
        val meta = doc.metadata
        assertEquals("karel_en_martijn.txt", meta.name)
        assertEquals(DocumentFormat.Txt, meta.format)
        assert(meta.text.contains("Fraaie historie ende alwaer"))
        val total = meta.annotations.annotations[Annotation.TOKEN]
        assertEquals(39, total)
    }

    @Test
    fun `Properties for an annotated file`() {
        val path = "formats/shared/converter/karel_en_martijn.tei.xml"
        val doc = TestUtil.getDoc(path)
        val meta = doc.metadata
        assertEquals("karel_en_martijn.tei.xml", meta.name)
        assertEquals(DocumentFormat.TeiP5, meta.format)
        assert(meta.text.contains("Fraaie historie ende alwaer"))
        val total = meta.annotations.annotations[Annotation.TOKEN]
        assertEquals(52, total)
    }
}