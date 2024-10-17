package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.upos
import org.ivdnt.galahad.evaluation.comparison.TermComparison

interface MetricsSettings {
    /** When are terms equal? */
    fun termsEqual(comp: TermComparison): Boolean

    /** What to group terms by */
    fun groupBy(term: Term): String

    /** What terms to keep in the metrics. Keep = return true */
    fun filterBy(term: TermComparison): Boolean = true

    @get:JsonIgnore
    val nullTerm: String

    @get:JsonIgnore
    val requiredAnnotations: List<AnnotationType>

    @get:JsonProperty("id")
    val id: String

    @get:JsonProperty("annotation")
    val annotation: String

    @get:JsonProperty("group")
    val group: String

    @get:JsonIgnore
    val groupAnnotation: AnnotationType
}

open class PosByPosMetricsSettings : MetricsSettings {
    override val id: String = "posByPos"
    override val annotation: String = "PoS"
    override val group: String = "PoS"
    override val groupAnnotation = AnnotationType.POS
    override val nullTerm: String = "NO_POS"
    override val requiredAnnotations = listOf(AnnotationType.POS)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.POS)
    }

    override fun groupBy(term: Term): String {
        return term.posHeadGroup ?: nullTerm
    }
}

class MultiPosByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "multiPosByPos"
    override val annotation: String = "PoS (multiple)"
    override fun filterBy(term: TermComparison): Boolean {
        return term.refTerm.isMulti(AnnotationType.POS)
    }
}

class SinglePosByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "singlePosByPos"
    override val annotation: String = "PoS (single)"
    override fun filterBy(term: TermComparison): Boolean {
        return !term.refTerm.isMulti(AnnotationType.POS)
    }
}

open class LemmaByLemmaMetricsSettings : MetricsSettings {
    override val id: String = "lemmaByLemma"
    override val annotation: String = "Lemma"
    override val group: String = "Lemma"
    override val groupAnnotation = AnnotationType.LEMMA
    override val nullTerm: String = "NO_LEMMA"
    override val requiredAnnotations = listOf(AnnotationType.LEMMA)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.LEMMA)
    }

    override fun groupBy(term: Term): String {
        return term.lemma ?: nullTerm
    }
}

open class DeprelByDeprel : MetricsSettings {
    override val id: String = "deprelByDeprel"
    override val annotation: String = "Deprel"
    override val group: String = "Deprel"
    override val groupAnnotation = AnnotationType.DEPREL
    override val nullTerm: String = "NO_DEPREL"
    override val requiredAnnotations = listOf(AnnotationType.DEPREL)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.DEPREL)
    }

    override fun groupBy(term: Term): String {
        return term.annotations[AnnotationType.DEPREL] ?: nullTerm
    }
}

class HeadByHead : MetricsSettings {
    override val id: String = "headByHead"
    override val annotation: String = "Head"
    override val group: String = "Head"
    override val groupAnnotation = AnnotationType.HEAD
    override val nullTerm: String = "NO_HEAD"
    override val requiredAnnotations = listOf(AnnotationType.HEAD)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.HEAD)
    }

    override fun groupBy(term: Term): String {
        return term.annotations[AnnotationType.HEAD] ?: nullTerm
    }
}

class MultiLemmaByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "multiLemmaByLemma"
    override val annotation: String = "Lemma (multiple)"
    override fun filterBy(term: TermComparison): Boolean {
        return term.refTerm.lemma?.contains("+") ?: false
    }
}

class SingleLemmaByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "singleLemmaByLemma"
    override val annotation: String = "Lemma (single)"
    override fun filterBy(term: TermComparison): Boolean {
        val isMulti = term.refTerm.lemma?.contains("+")  ?: false
        return !isMulti
    }
}

class LemmaByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "lemmaByPos"
    override val annotation: String = "Lemma"
    override val requiredAnnotations = listOf(AnnotationType.LEMMA, AnnotationType.POS)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.LEMMA)
    }
}

class PosByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "posByLemma"
    override val annotation: String = "PoS"
    override val requiredAnnotations = listOf(AnnotationType.LEMMA, AnnotationType.POS)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.POS)
    }
}

class LemmaPosByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "lemmaPosByPos"
    override val annotation: String = "Lemma + PoS"
    override val requiredAnnotations = listOf(AnnotationType.LEMMA, AnnotationType.POS)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.POS) && comp.equalAnnotation(AnnotationType.LEMMA)
    }
}

class DeprelHeadbyDeprelMetricsSettings : DeprelByDeprel() {
    override val id: String = "deprelHeadByDeprel"
    override val annotation: String = "Deprel + Head"
    override val requiredAnnotations = listOf(AnnotationType.DEPREL, AnnotationType.HEAD)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.DEPREL) && comp.equalAnnotation(AnnotationType.HEAD)
    }
}

class LemmaPosByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "lemmaPosByLemma"
    override val annotation: String = "Lemma + PoS"
    override val requiredAnnotations = listOf(AnnotationType.LEMMA, AnnotationType.POS)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.POS) && comp.equalAnnotation(AnnotationType.LEMMA)
    }
}

class UposByUposMetricsSettings : MetricsSettings {
    override val id: String = "uposByUpos"
    override val annotation: String = "upos"
    override val group: String = "upos"
    override val groupAnnotation = AnnotationType.UPOS
    override val nullTerm: String = "NO_UPOS"
    override val requiredAnnotations = listOf(AnnotationType.UPOS)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.UPOS)
    }

    override fun groupBy(term: Term): String {
        return Term.annotationToHead(term.annotations.upos) ?: nullTerm
    }
}

class NerByNerMetricsSettings : MetricsSettings {
    override val id: String = "named-entityByNamed-entity"
    override val annotation: String = "named-entity"
    override val group: String = "named-entity"
    override val groupAnnotation = AnnotationType.NER
    override val nullTerm: String = "NO_NAMED_ENTITY"
    override val requiredAnnotations = listOf(AnnotationType.NER)

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalAnnotation(AnnotationType.NER)
    }

    override fun groupBy(term: Term): String {
        return Term.annotationToHead(term.annotations[AnnotationType.NER]) ?: nullTerm
    }
}

/** Used by [Metrics] to instantiate a [MetricsType] for each setting. */
val METRIC_TYPES = listOf(
    // Pos
    PosByPosMetricsSettings(),
    PosByLemmaMetricsSettings(),
    MultiPosByPosMetricsSettings(),
    SinglePosByPosMetricsSettings(),
    // Lemma
    LemmaByLemmaMetricsSettings(),
    LemmaByPosMetricsSettings(),
    MultiLemmaByLemmaMetricsSettings(),
    SingleLemmaByLemmaMetricsSettings(),
    // Lemma + Pos
    LemmaPosByPosMetricsSettings(),
    LemmaPosByLemmaMetricsSettings(),
    // UD
    DeprelByDeprel(),
    HeadByHead(),
    UposByUposMetricsSettings(),
    DeprelHeadbyDeprelMetricsSettings(),
    NerByNerMetricsSettings()
)