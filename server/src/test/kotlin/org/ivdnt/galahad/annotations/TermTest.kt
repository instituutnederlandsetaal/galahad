package org.ivdnt.galahad.annotations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TermTest {
    // Only the relevant parameters are initialized.
    private val singleTarget = Term(
        "", "", mutableListOf(WordForm("schooltje", 0, 0, "0"))
    )
    private val multiTarget = Term(
        "", "", mutableListOf(
            WordForm("hoogbouw", 0, 0, "0"), WordForm("en", 0, 0, "0"), WordForm("laagbouw", 0, 0, "0")
        )
    )

    // upos
    private val singleUPos = Term(
        mapOf(
            AnnotationType.UPOS to "NOU-C(num=sg)"
        ), mutableListOf()
    )

    // ner
    private val singleNer = Term(
        mapOf(
            AnnotationType.NER to "B-LOC"
        ), mutableListOf()
    )

    // pos
    private val singlePos = Term(
        mapOf(
            AnnotationType.POS to "NOU-C(num=sg)"
        ), mutableListOf()
    )

    private val multiPos = Term(
        mapOf(
            AnnotationType.POS to "PD(type=art)+NOU-C(num=sg)"
        ), mutableListOf()
    )

    private val headOnlyPos = Term(
        mapOf(
            AnnotationType.POS to "NOU-C"
        ), mutableListOf()
    )


    @Test
    fun `PoS head and features`() {
        // single pos
        assertEquals("NOU-C", singlePos.annotationHead(AnnotationType.POS))
        assertEquals("num=sg", Term.features(singlePos.annotations.pos))

        // upos
        assertEquals("NOU-C", singleUPos.annotationHead(AnnotationType.UPOS))
        assertEquals("num=sg", Term.features(singleUPos.annotations.upos))

        // ner
        assertEquals("LOC", singleNer.annotationHead(AnnotationType.NER))

        // multi pos
        assertEquals("PD+NOU-C", multiPos.annotationHead(AnnotationType.POS))
        // no features for now...

        // head only pos
        assertEquals("NOU-C", headOnlyPos.annotationHead(AnnotationType.POS))
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