package org.ivdnt.galahad.documents

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FormatInducerTest {
    @Test
    fun `Parse simple formats based on extension`() {
        assertEquals(DocumentFormat.Tsv, DocumentFormat.fromFile(TestUtil.get("formats/shared/converter/karel_en_martijn.tsv")))
        assertEquals(DocumentFormat.Conllu, DocumentFormat.fromFile(TestUtil.get("formats/shared/converter/karel_en_martijn.conllu")))
        assertEquals(DocumentFormat.Txt, DocumentFormat.fromFile(TestUtil.get("formats/shared/converter/karel_en_martijn.txt")))
    }

    @Test
    fun `Parse invalid formats`() {
        assertThrows(Exception::class.java) { DocumentFormat.fromString("invalid") }
    }

    @Test
    fun `Parse non-legacy XML formats`() {
        assertEquals(DocumentFormat.Folia, DocumentFormat.fromFile(TestUtil.get("formats/shared/converter/karel_en_martijn.folia.xml")))
        assertEquals(DocumentFormat.TeiP5, DocumentFormat.fromFile(TestUtil.get("formats/shared/converter/karel_en_martijn.tei.xml")))
        assertEquals(DocumentFormat.TeiP5, DocumentFormat.fromFile(TestUtil.get("formats/tei/reader/teicorpus/input.tei.xml")))
        assertEquals(DocumentFormat.Naf, DocumentFormat.fromFile(TestUtil.get("formats/shared/converter/karel_en_martijn.naf.xml")))
    }

    @Test
    fun `Parse legacy XML formats`() {
        assertEquals(DocumentFormat.TeiP4Legacy, DocumentFormat.fromFile(TestUtil.get("formats/tei/reader/teip4/input.tei.xml")))

    }

    @Test
    fun `Parse unknown XML root node`() {
        assertThrows(Exception::class.java) { DocumentFormat.fromFile(TestUtil.get("documents/invalid-root.xml")) }
    }
}