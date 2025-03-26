package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.export.csv.CSVFile
import org.ivdnt.galahad.export.csv.CSVHeader
import org.ivdnt.galahad.taggers.Tagger


const val TRUNCATE: Int = 100

/**
 * Generic class for benchmark [Metric]s of a corpus or document.
 * The idea is to sum up the distribution as we go through the terms one by one using [add].
 */
open class Metrics(
    corpus: Corpus,
    @JsonIgnore val settings: List<MetricsSettings>,
    val hypothesis: String,
    val reference: String,
    @JsonIgnore val truncate: Boolean = true,
) {
    @JsonProperty("metrics")
    val metricTypes: MutableMap<String, MetricsType> = HashMap()

    val hypoTagger: Tagger = Tagger.readOrThrow(hypothesis, corpus)
    val refTagger: Tagger = Tagger.readOrThrow(reference, corpus)
    protected val hypothesisJob = corpus.jobs.readOrThrow(hypothesis)
    protected val referenceJob = corpus.jobs.readOrThrow(reference)

    init {
        settings.forEach { metricTypes[it.id] = MetricsType(it, hypoTagger, refTagger).also { it.truncate = truncate } }
    }

    fun toGlobalCsv(): String {
        var csv: String = getCsvHeader()
        metricTypes.values.forEach { csv += it.toGlobalCsv() }
        return csv
    }

    protected fun add(other: Metrics) {
        other.metricTypes.values.toSet().forEach { metricTypes[it.setting.id]?.add(it) }
    }

    fun add(comp: TermComparison) {
        settings.forEach { metricTypes[it.id]?.add(comp) }
    }

    companion object {
        fun getCsvHeader(): CSVHeader {
            return CSVFile.toCSVHeader(
                listOf(
                    "annotation",
                    "grouped by",
                    "macro precision",
                    "macro recall",
                    "macro f1",
                    "micro accuracy",
                    "count",
                    "true positive count",
                    "false negative count",
                    "no match count"
                )
            )
        }
    }
}
