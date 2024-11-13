package org.ivdnt.galahad.data.layer

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

    private val multiPos = Term("", "PD(type=art)+NOU-C(num=sg)", mutableListOf())
    private val singlePos = Term("", "NOU-C(num=sg)", mutableListOf())
    private val headOnlyPos = Term("", "NOU-C", mutableListOf())



    @Test
    fun `PoS head and features`() {
        // single pos
        assertEquals("NOU-C", singlePos.annotationHead(AnnotationType.POS))
        assertEquals("num=sg", Term.features(singlePos.pos))

        // multi pos
        assertEquals("PD+NOU-C", multiPos.annotationHead(AnnotationType.POS))
        // no features for now...

        // head only pos
        assertEquals("NOU-C", headOnlyPos.annotationHead(AnnotationType.POS))
        assertEquals(null, Term.features(headOnlyPos.pos))
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