package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.lemma
import org.ivdnt.galahad.annotations.pos
import org.ivdnt.galahad.annotations.token
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class TSVHeaderTest {
    @Test
    fun `Parse literal names in header`() {
        // This folder contains TSV files with various names for the literal in the headers.
        for (file in File("src/test/resources/tsv/literal-name-headers").listFiles()!!) {
            val tsvFile = TsvFile(file)
            tsvFile.parse()
            // The column positions are fixed
            assertEquals(tsvFile.columnIndices[Annotation.TOKEN], 0)
            assertEquals(tsvFile.columnIndices[Annotation.LEMMA], 1)
            assertEquals(tsvFile.columnIndices[Annotation.POS], 2)
            // Check entries
            assertEquals(tsvFile.entries.size, 1)
            tsvFile.entries.forEach {
                assertEquals("scholen", it.token)
                assertEquals("school", it.lemma)
                assertEquals("NOU", it.pos)
            }
        }
    }

    @Test
    fun `Parse a tsv file with all annotation type columns`() {
        val tsvFile = TsvFile(File("src/test/resources/tsv/header-all-annotation-types/input.tsv"))
        tsvFile.parse()
        var layer = tsvFile.layer
        assertEquals(2, layer.terms.size)
        assertEquals(6, layer.terms[0].annotations.size)
        assertEquals(6, layer.terms[1].annotations.size)
    }

    @Test
    fun `Parse header column orders`() {
        // This folder contains TSV files with various column orders in the headers.
        for (file in File("src/test/resources/tsv/header-order").listFiles()!!) {
            val tsvFile = TsvFile(file)
            tsvFile.parse()
            // The column positions change, so no checks here.
            // Instead, check entries.
            assertEquals(1, tsvFile.entries.size)
            tsvFile.entries.forEach {
                assertEquals("scholen", it.token)
                assertEquals("school", it.lemma)
                assertEquals("NOU", it.pos)
            }
        }
    }

    @Test
    fun `Incorrect headers`() {
        // This folder contains TSV files with incorrect headers.
        val tsvFile = TsvFile(File("src/test/resources/tsv/incorrect-headers/empty-word.tsv"))
        assertThrows<Exception> { tsvFile.parse() }
    }
}