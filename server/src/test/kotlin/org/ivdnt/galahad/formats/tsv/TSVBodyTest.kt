package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class TSVBodyTest {
    @Test
    fun `Skip empty lines`() {
        val tsvFile = TsvFile(File("src/test/resources/tsv/body/emptylines.tsv"))
        assertTSVFile(tsvFile)
    }

    // Technically, tsv files are not supposed to have comments,
    // but some corpora use # above a sentence/document to indicate provenance.
    @Test
    fun `Skip comments`() {
        val tsvFile = TsvFile(File("src/test/resources/tsv/body/comments.tsv"))
        assertTSVFile(tsvFile)
    }

    @Test
    fun `Body with extra columns`() {
        val tsvFile = TsvFile(File("src/test/resources/tsv/body/extra-columns.tsv"))
        assertTSVFile(tsvFile)
        val expected = "scholen loop " // Note the space.
        assertEquals(expected, tsvFile.layer.toString())
    }

    @Test
    fun `Missing values`() {
        // Contains 3 files with a missing value for lemma, pos and literal.
        for (file in File("src/test/resources/tsv/body-missing-values").listFiles()!!) {
            val tsvFile = TsvFile(file)
            val type = file.nameWithoutExtension.split("-")[1]
            val entries = tsvFile.layer.terms
            when (type) {
                // Empty string when lemma or pos is missing.
                "lemma" -> assertEquals(null, entries.first().lemma)
                "pos" -> assertEquals(null, entries.first().pos)
                // No entries when literal is missing.
                // After all, this would not generate plaintext.
                "word" -> assertEquals(0, entries.count())
            }
        }
    }

    // The files in the body/ folder have the same content, so reuse the same test.
    private fun assertTSVFile(tsvFile: TsvFile) {
        assertEntries(tsvFile.layer.terms.toList())
        assertSourceLayer(tsvFile.layer)
    }

    private fun assertEntries(entries: List<Term>) {
        assertEquals(2, entries.size)
        val first = entries[0]
        assertEquals("scholen", first.token)
        assertEquals("school", first.lemma)
        assertEquals("NOU", first.pos)
        val second = entries[1]
        assertEquals("loop", second.token)
        assertEquals("lopen", second.lemma)
        assertEquals("VRB", second.pos)
    }

    private fun assertSourceLayer(layer: Layer) {
        // count
        assertEquals(2, layer.terms.count())
        // wordforms
        assertWordFormAndTerm(layer, 0, "scholen", "school", "NOU")
        assertWordFormAndTerm(layer, 1, "loop", "lopen", "VRB")
    }

    // We don't assert offsets here, because they will vary depending on the file due to newlines and such.
    private fun assertWordFormAndTerm(
        layer: Layer, i: Int, literal: String, lemma: String, pos: String,
    ) {
        val term = layer.terms.elementAt(i)
        assertEquals(literal, term.token)
        assertEquals("w$i", term.id)
        assertEquals(lemma, term.lemma)
        assertEquals(pos, term.pos)
        assertEquals(null, Term.features(term.pos))
        assertEquals(pos, term.annotationHead(Annotation.POS))
    }
}