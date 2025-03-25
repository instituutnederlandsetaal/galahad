package org.ivdnt.galahad.annotations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TermTest {
    // Only the relevant parameters are initialized.
    private val singleTarget = Term(
        "", "", mutableListOf(WordForm("schooltje", 0,  "0"))
    )
    private val multiTarget = Term(
        "", "", mutableListOf(
            WordForm("hoogbouw", 0,  "0"), WordForm("en", 0,  "0"), WordForm("laagbouw", 0,  "0")
        )
    )

    // upos
    private val singleUPos = Term(
        mapOf(
            Annotation.UPOS to "NOU-C(num=sg)"
        ), mutableListOf()
    )

    // ner
    private val singleNer = Term(
        mapOf(
            Annotation.NER to "B-LOC"
        ), mutableListOf()
    )

    // pos
    private val singlePos = Term(
        mapOf(
            Annotation.POS to "NOU-C(num=sg)"
        ), mutableListOf()
    )

    private val multiPos = Term(
        mapOf(
            Annotation.POS to "PD(type=art)+NOU-C(num=sg)"
        ), mutableListOf()
    )

    private val headOnlyPos = Term(
        mapOf(
            Annotation.POS to "NOU-C"
        ), mutableListOf()
    )


    @Test
    fun `PoS head and features`() {
        // single pos
        assertEquals("NOU-C", singlePos.annotationHead(Annotation.POS))
        assertEquals("num=sg", Term.features(singlePos.annotations.pos))

        // upos
        assertEquals("NOU-C", singleUPos.annotationHead(Annotation.UPOS))
        assertEquals("num=sg", Term.features(singleUPos.annotations.upos))

        // ner
        assertEquals("LOC", singleNer.annotationHead(Annotation.NER))

        // multi pos
        assertEquals("PD+NOU-C", multiPos.annotationHead(Annotation.POS))
        // no features for now...

        // head only pos
        assertEquals("NOU-C", headOnlyPos.annotationHead(Annotation.POS))
        assertEquals(null, Term.features(headOnlyPos.annotations.pos))
    }

    @Test
    fun `Literals for multi target term`() {
        assertEquals("hoogbouw en laagbouw", multiTarget.literals)
    }

    @Test
    fun `Literals for single target term`() {
        assertEquals("schooltje", singleTarget.literals)
    }
}