package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.evaluation.frequency.TokenFrequency

// for compound lemma like "aan_pakken" instead of "aanpakken"
val LEMMA_REGEX = Regex("_")

interface MetricsSettings {
    /** When are terms equal? */
    fun termsEqual(comp: TermComparison): Boolean

    /** What to group terms by */
    fun groupBy(term: Term): String

    /** What terms to keep in the metrics. Keep = return true */
    fun filterBy(term: TermComparison): Boolean = true

    @get:JsonIgnore
    val hasFalsePositive: Boolean
        get() = true

    @get:JsonIgnore val nullTerm: String

    @get:JsonIgnore val requiredAnnotations: List<Annotation>

    @get:JsonProperty("id") val id: String

    @get:JsonProperty("annotation") val annotation: String

    @get:JsonProperty("group") val group: String

    @get:JsonIgnore val groupAnnotation: Annotation
}

open class PosByPosMetricsSettings : MetricsSettings {
    override val id: String = "posByPos"
    override val annotation: String = "PoS"
    override val group: String = "PoS"
    override val groupAnnotation: Annotation = Annotation.POS
    override val nullTerm: String = "NO_POS"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.POS)

    override fun termsEqual(comp: TermComparison): Boolean = comp.equal(Annotation.POS)

    override fun groupBy(term: Term): String = term.annotationHeadOrMissing(Annotation.POS)
}

class MultiPosByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "multiPosByPos"
    override val annotation: String = "PoS (multiple)"

    override fun filterBy(term: TermComparison): Boolean = term.ref.isMulti(Annotation.POS)
}

class SinglePosByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "singlePosByPos"
    override val annotation: String = "PoS (single)"

    override fun filterBy(term: TermComparison): Boolean = !term.ref.isMulti(Annotation.POS)
}

open class LemmaByLemmaMetricsSettings : MetricsSettings {
    override val id: String = "lemmaByLemma"
    override val annotation: String = "Lemma"
    override val group: String = "Lemma"
    override val groupAnnotation: Annotation = Annotation.LEMMA
    override val nullTerm: String = "NO_LEMMA"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.LEMMA)

    override fun termsEqual(comp: TermComparison): Boolean =
        comp.equal(Annotation.LEMMA, LEMMA_REGEX)

    override fun groupBy(term: Term): String = term.lemma ?: nullTerm
}

open class DeprelByDeprel : MetricsSettings {
    override val id: String = "deprelByDeprel"
    override val annotation: String = "Deprel"
    override val group: String = "Deprel"
    override val groupAnnotation: Annotation = Annotation.DEPREL
    override val nullTerm: String = "NO_DEPREL"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.DEPREL)

    override fun termsEqual(comp: TermComparison): Boolean = comp.equal(Annotation.DEPREL)

    override fun groupBy(term: Term): String = term.annotations[Annotation.DEPREL] ?: nullTerm
}

class HeadByHead : MetricsSettings {
    override val id: String = "headByHead"
    override val annotation: String = "Head"
    override val group: String = "Head"
    override val groupAnnotation: Annotation = Annotation.HEAD
    override val nullTerm: String = "NO_HEAD"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.HEAD)

    override fun termsEqual(comp: TermComparison): Boolean = comp.equal(Annotation.HEAD)

    override fun groupBy(term: Term): String = term.annotations[Annotation.HEAD] ?: nullTerm
}

class MultiLemmaByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "multiLemmaByLemma"
    override val annotation: String = "Lemma (multiple)"

    override fun filterBy(term: TermComparison): Boolean = term.ref.isMulti(Annotation.LEMMA)
}

class SingleLemmaByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "singleLemmaByLemma"
    override val annotation: String = "Lemma (single)"

    override fun filterBy(term: TermComparison): Boolean {
        val isMulti = term.ref.isMulti(Annotation.LEMMA)
        return !isMulti
    }
}

class LemmaByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "lemmaByPos"
    override val annotation: String = "Lemma"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.LEMMA, Annotation.POS)

    override fun termsEqual(comp: TermComparison): Boolean =
        comp.equal(Annotation.LEMMA, LEMMA_REGEX)
}

class PosByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "posByLemma"
    override val annotation: String = "PoS"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.LEMMA, Annotation.POS)

    override fun termsEqual(comp: TermComparison): Boolean = comp.equal(Annotation.POS)
}

class LemmaPosByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "lemmaPosByPos"
    override val annotation: String = "Lemma + PoS"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.LEMMA, Annotation.POS)

    override fun termsEqual(comp: TermComparison): Boolean =
        comp.equal(Annotation.POS) && comp.equal(Annotation.LEMMA, LEMMA_REGEX)
}

class DeprelHeadbyDeprelMetricsSettings : DeprelByDeprel() {
    override val id: String = "deprelHeadByDeprel"
    override val annotation: String = "Deprel + Head"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.DEPREL, Annotation.HEAD)

    override fun termsEqual(comp: TermComparison): Boolean =
        comp.equal(Annotation.DEPREL) && comp.equal(Annotation.HEAD)
}

class LemmaPosByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "lemmaPosByLemma"
    override val annotation: String = "Lemma + PoS"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.LEMMA, Annotation.POS)

    override fun termsEqual(comp: TermComparison): Boolean =
        comp.equal(Annotation.POS) && comp.equal(Annotation.LEMMA, LEMMA_REGEX)
}

class UposByUposMetricsSettings : MetricsSettings {
    override val id: String = "uposByUpos"
    override val annotation: String = "upos"
    override val group: String = "upos"
    override val groupAnnotation: Annotation = Annotation.UPOS
    override val nullTerm: String = "NO_UPOS"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.UPOS)

    override fun termsEqual(comp: TermComparison): Boolean = comp.equal(Annotation.UPOS)

    override fun groupBy(term: Term): String = term.annotationHeadOrMissing(Annotation.UPOS)
}

class NerByNerMetricsSettings : MetricsSettings {
    override val id: String = "named-entityByNamed-entity"
    override val annotation: String = "named-entity"
    override val group: String = "named-entity"
    override val groupAnnotation: Annotation = Annotation.NER
    override val nullTerm: String = "NO_NAMED_ENTITY"
    override val requiredAnnotations: List<Annotation> = listOf(Annotation.NER)

    override fun termsEqual(comp: TermComparison): Boolean = comp.equal(Annotation.NER)

    override fun groupBy(term: Term): String = term.annotationHeadOrMissing(Annotation.NER)
}

class FrequencyMetricsSettings(
    private val tokenFrequency: TokenFrequency,
    private val metric: MetricsSettings,
) : MetricsSettings {
    override val id: String = "${metric.annotation}ByFrequency"
    override val annotation: String = metric.annotation
    override val group: String = "frequency"
    override val groupAnnotation: Annotation = metric.groupAnnotation
    override val nullTerm: String = metric.nullTerm
    override val requiredAnnotations: List<Annotation> = metric.requiredAnnotations
    override val hasFalsePositive: Boolean
        get() = false

    override fun termsEqual(comp: TermComparison): Boolean = metric.termsEqual(comp)

    override fun groupBy(term: Term): String {
        val freq = tokenFrequency.getFrequency(term.token.lowercase())
        val truncatedFreq = tokenFrequency.getFrequency(term.token.lowercase())
        return if (freq == 0) {
            truncatedFreq.toString()
        } else {
            return freq.toString()
        }
    }
}

/** Used by [DocumentMetric] to instantiate each setting. */
val METRIC_TYPES: List<MetricsSettings> =
    listOf(
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
        NerByNerMetricsSettings(),
    )
