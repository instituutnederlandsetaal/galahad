package org.ivdnt.galahad.documents

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.util.TestUtil.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DocumentMetadataTest() {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
        val files = get("formats/shared/converter").listFiles()
        files.forEach { corpus.documents.createOrThrow(it) }
    }

    @Test
    fun `Properties for an unannotated file`() {
        val meta = corpus.documents.readOrThrow("karel_en_martijn.txt").metadata
        assertEquals("karel_en_martijn.txt", meta.name)
        assertEquals(DocumentFormat.Txt, meta.format)
        assert(meta.text.contains("Fraaie historie ende alwaer"))
        val total = meta.annotations.annotations[Annotation.TOKEN]
        assertEquals(39, total)
    }

    @Test
    fun `Properties for an annotated file`() {
        val meta = corpus.documents.readOrThrow("karel_en_martijn.tei.xml").metadata
        assertEquals("karel_en_martijn.tei.xml", meta.name)
        assertEquals(DocumentFormat.TeiP5, meta.format)
        assert(meta.text.contains("Fraaie historie ende alwaer"))
        val total = meta.annotations.annotations[Annotation.TOKEN]
        assertEquals(52, total)
    }
}
