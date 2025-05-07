package org.ivdnt.galahad.data.documents

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FormatInducerTest {
    @Test
    fun `Parse simple formats based on extension`() {
        assertEquals(DocumentFormat.Tsv, DocumentFormat.fromFile(TestUtil.get("all-formats/input/input.tsv")))
        assertEquals(DocumentFormat.Conllu, DocumentFormat.fromFile(TestUtil.get("all-formats/input/input.conllu")))
        assertEquals(DocumentFormat.Txt, DocumentFormat.fromFile(TestUtil.get("all-formats/input/input.txt")))
    }

    @Test
    fun `Parse invalid formats`() {
        assertEquals(DocumentFormat.Unknown, DocumentFormat.fromFile(TestUtil.get("all-formats/invalid/invalid")))
        assertEquals(DocumentFormat.Unknown, DocumentFormat.fromFile(TestUtil.get("all-formats/invalid/invalid.json")))
        assertThrows(Exception::class.java) { DocumentFormat.fromString("invalid") }
    }

    @Test
    fun `Parse non-legacy XML formats`() {
        assertEquals(DocumentFormat.Folia, DocumentFormat.fromFile(TestUtil.get("all-formats/input/input.folia.xml")))
        assertEquals(DocumentFormat.TeiP5, DocumentFormat.fromFile(TestUtil.get("all-formats/input/input.tei.xml")))
        assertEquals(DocumentFormat.Naf, DocumentFormat.fromFile(TestUtil.get("all-formats/input/input.naf.xml")))
    }

    @Test
    fun `Parse legacy XML formats`() {
        assertEquals(DocumentFormat.TeiP4Legacy, DocumentFormat.fromFile(TestUtil.get("all-formats/tei/teip4legacy.xml")))
        // if 1 or more pos are present, it's TeiP5
        assertEquals(DocumentFormat.TeiP5, DocumentFormat.fromFile(TestUtil.get("all-formats/tei/1-pos-0-type.tei.xml")))
        // even if there are types, it's still TeiP5
        assertEquals(DocumentFormat.TeiP5, DocumentFormat.fromFile(TestUtil.get("all-formats/tei/1-pos-1-type.tei.xml")))
        // if no pos or type are present, it's unannotated and we default to TeiP5
        assertEquals(DocumentFormat.TeiP5, DocumentFormat.fromFile(TestUtil.get("all-formats/tei/0-pos-0-type.tei.xml")))
        // if no pos are present, but at least one type is present, it's TeiP5Legacy
        assertEquals(DocumentFormat.TeiP5Legacy, DocumentFormat.fromFile(TestUtil.get("all-formats/tei/0-pos-1-type.tei.xml")))

    }

    @Test
    fun `Parse unknown XML root node`() {
        assertEquals(DocumentFormat.Unknown, DocumentFormat.fromFile(TestUtil.get("all-formats/invalid/invalid.xml")))
    }
}