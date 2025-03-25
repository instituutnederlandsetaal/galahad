package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.annotations.DocumentLayer
import org.ivdnt.galahad.annotations.ParagraphLayer
import org.ivdnt.galahad.annotations.SentenceLayer
import org.ivdnt.galahad.formats.Resource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TxtReaderTest {
    @Test
    fun `Read txt file`() {
        val reader = TxtReader(Resource.get("txt/input.txt"))
        val layer = reader.layer

        val docs = layer.documents
        assertEquals(1, docs.size)
        assertDocument(docs[0], "d1", 3)
        val d1p = docs[0].paragraphs

        val d1p1 = d1p[0]
        assertParagraph(d1p1, 2)
        val d1p1s = d1p[0].sentences
        assertSentence(d1p1s[0], 4) // Fraaie historie ende alwaer.
        assertSentence(d1p1s[1], 6) // Magh 'k u vertellen, hoirt naer.

        val d1p2 = d1p[1]
        assertParagraph(d1p2, 2)
        val d1p2s = d1p[1].sentences
        assertSentence(d1p2s[0], 5) // 't Was op enen avondstonde.
        assertSentence(d1p2s[1], 5) // Dat koning Carel slaepen beghonde.

        val d1p3 = d1p[2]
        assertParagraph(d1p3, 4)
        val d1p3s = d1p[2].sentences
        assertSentence(d1p3s[0], 4) // tEngelem aen den Ryn.
        assertSentence(d1p3s[1], 5) // 't Landt was algader syn.
        // Hi was keiser ende coninc mede. Hoirt hier wonder ende waerhede. Wat de coninc daer gevel.
        assertSentence(d1p3s[2], 16)
        assertSentence(d1p3s[3], 6) // Dat weten de menighen noch wel.
    }

    private fun assertDocument(doc: DocumentLayer, id: String, size: Int) {
        assertEquals(id, doc.id)
        assertEquals(size, doc.paragraphs.size)
        doc.paragraphs.forEachIndexed { i, p -> assertEquals("p${i + 1}", p.id) }
    }

    private fun assertParagraph(par: ParagraphLayer, size: Int) {
        assertEquals(size, par.sentences.size)
        par.sentences.forEachIndexed { i, s -> assertEquals("s${i + 1}", s.id) }
    }

    private fun assertSentence(sent: SentenceLayer, size: Int) {
        assertEquals(size, sent.terms.size)
        sent.terms.forEachIndexed { i, t -> assertEquals("w${i + 1}", t.id) }
    }
}