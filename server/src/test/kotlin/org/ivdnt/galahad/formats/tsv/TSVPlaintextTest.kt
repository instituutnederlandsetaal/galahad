package org.ivdnt.galahad.formats.tsv

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class TSVPlaintextTest {
    @Test
    fun `Parsed plaintext and mapping`() {
        val tsvFile = TsvFile(File("src/test/resources/tsv/plaintext/peerle.tsv"))
        // Parsing creates entries and plaintext.
        assertEquals(1812, tsvFile.layer.terms.count())
        // Assert plaintext.
        val plainText = File("src/test/resources/tsv/plaintext/peerle.txt").readText()
        assertEquals(plainText, tsvFile.layer.toString())
    }
}