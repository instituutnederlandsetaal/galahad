package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.formats.InternalFile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class TSVHeaderTest {
    @Test
    fun `Parse literal names in header`() {
        // This folder contains TSV files with various names for the literal in the headers.
        for (file in File("src/test/resources/tsv/literal-name-headers").listFiles()!!) {
            val tsvFile: InternalFile = TsvFile(file)
            // Check entries
            assertEquals(tsvFile.layer.terms.count(), 1)
            tsvFile.layer.terms.forEach {
                assertEquals("scholen", it.token)
                assertEquals("school", it.lemma)
                assertEquals("NOU", it.pos)
            }
        }
    }

    @Test
    fun `Parse a tsv file with all annotation type columns`() {
        val tsvFile = TsvFile(File("src/test/resources/tsv/header-all-annotation-types/input.tsv"))
        val layer = tsvFile.layer
        assertEquals(2, layer.terms.count())
        assertEquals(6, layer.terms.first().annotations.size)
        assertEquals(6, layer.terms.elementAt(1).annotations.size)
    }

    @Test
    fun `Parse header column orders`() {
        // This folder contains TSV files with various column orders in the headers.
        for (file in File("src/test/resources/tsv/header-order").listFiles()!!) {
            val tsvFile = TsvFile(file)
            // The column positions change, so no checks here.
            // Instead, check entries.
            assertEquals(1, tsvFile.layer.terms.count())
            tsvFile.layer.terms.forEach {
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
        assertThrows<Exception> { tsvFile.layer }
    }
}