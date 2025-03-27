package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.CsvSampleExporter
import org.ivdnt.galahad.evaluation.EvaluationEntry
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.exceptions.InvalidClassificationTypeException
import org.ivdnt.galahad.export.csv.CSVFile
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.toFixed

enum class ClassificationType(val value: String) {
    TRUE_POSITIVE("truePositive"),
    FALSE_POSITIVE("falsePositive"),
    FALSE_NEGATIVE("falseNegative"),
    NO_MATCH("noMatch");

    companion object {
        fun fromString(s: String): ClassificationType =
            entries.firstOrNull { it.value == s } ?: throw InvalidClassificationTypeException(
                "Invalid value $s, valid values are ${entries.map { it.value }}"
            )
    }
}

typealias FlatMetricTypeAssay = Map<String, FlatMetricType>

class FlatMetricType(
    val micro: ClassificationMetrics = ClassificationMetrics(),
    val macro: ClassificationMetrics = ClassificationMetrics(),
)

class MetricsType(
    val setting: MetricsSettings,
    val hypothesis: Tagger,
    val reference: Tagger,
    @JsonIgnore var truncate: Boolean = true,
) : CsvSampleExporter {
    @JsonIgnore
    var map: MutableMap<String, Metric> = HashMap()

    /** Metrics separated per POS. */
    val grouped: Set<Metric>
        get() {
            return if (map.size > TRUNCATE) {
                // sort mt.value.map on mt.value.map["someKey"].cls.classCount, take the first TRUNCATE elements, and then map
                map.entries.asSequence()
                    .sortedByDescending { it.value.cls.classCount }.take(TRUNCATE)
                    .associateBy({ it.key }, { it.value }).values.toSet()
            } else {
                map.values.toSet()
            }
        }

    val classes: ClassificationClasses
        get() = map.values.map { it.cls }.toMutableList().apply { this.add(0, ClassificationClasses(count = 0)) }
            .reduce { a, b -> a.add(b, truncate) }.apply { falsePositive = EvaluationEntry() }

    private val macro: ClassificationMetrics
        get() {
            if (map.isEmpty()) {
                return ClassificationMetrics()
            }
            return map.values.map { it.clsMetrics }.reduce { a, b -> a + b } / map.size
        }

    private val micro: ClassificationMetrics
        get() {
            if (map.isEmpty()) {
                return ClassificationMetrics()
            }
            return ClassificationMetrics.calculate(
                map.values.map { it.cls.flat }.reduce { a, b -> a + b },
                micro = true
            )
        }

    fun toGlobalCsv(): String {
        // Expensive calculations.
        val microMetrics = micro
        val macroMetrics = macro

        return CSVFile.toCSVRecord(
            listOf(
                setting.annotation,
                setting.group,
                macroMetrics.precision.toFixed(),
                macroMetrics.recall.toFixed(),
                macroMetrics.f1.toFixed(),
                microMetrics.accuracy.toFixed(),
                classes.classCount,
                classes.truePositive.count,
                classes.falseNegative.count,
                classes.noMatch.count,
            )
        )
    }

    fun toFlat(): FlatMetricType = FlatMetricType(micro, macro)

    fun toGroupedCsv(): String {
        var csv = Metric.getCsvHeader()
        grouped.sortedBy { it.name }.forEach { csv += it.toCSVRecord() }
        return csv
    }

    // Cumulative addition functions.
    private fun add(metric: Metric) {
        map.merge(metric.name, metric) { m1, m2 -> m1.add(m2, truncate) }
    }

    fun add(other: MetricsType) {
        other.map.values.toSet().forEach(this::add)
    }

    fun add(comp: TermComparison) {
        if (!setting.filterBy(comp)) {
            return
        }

        // no match
        if (comp.hypoTerm == Term.EMPTY) {
            add(
                Metric(
                    name = setting.groupBy(comp.refTerm),
                    cls = ClassificationClasses(
                        noMatch = EvaluationEntry(1, mutableListOf(comp)),
                        count = 0
                    )
                )
            )
            return // don't show no matches in false positives / true negatives.
        }

        // One of these two will be empty, we don't know which.
        val (trues, falses) = truesFalses(comp, setting::termsEqual)
        val cls = ClassificationClasses(
            truePositive = trues,
            falseNegative = falses,
        )
        add(
            Metric(
                name = setting.groupBy(comp.refTerm),
                cls = cls
            )
        )
        if (falses.count != 0 && setting.hasFalsePositive) {
            // This term is also be someone else's false positive, so switch around.
            val cls2 = ClassificationClasses(
                falsePositive = EvaluationEntry(count = falses.samples.size, falses.samples.toMutableList()),
                count = if (setting.groupBy(comp.hypoTerm) == setting.groupBy(comp.refTerm)) 0 else 1
            )
            add(
                Metric(
                    name = setting.groupBy(comp.hypoTerm), // Terms are switched, so hypo.
                    cls = cls2
                )
            )
        }
    }

    private fun truesFalses(
        comp: TermComparison,
        cond: (TermComparison) -> Boolean,
    ): Pair<EvaluationEntry, EvaluationEntry> {
        val trues = if (cond(comp)) {
            EvaluationEntry(1, mutableListOf(comp))
        } else {
            EvaluationEntry()
        }
        val falses = if (!cond(comp)) {
            EvaluationEntry(1, mutableListOf(comp))
        } else {
            EvaluationEntry()
        }
        return Pair(trues, falses)
    }

    fun samplesToCsv(group: String, classType: ClassificationType): String {
        return when (classType) {
            ClassificationType.TRUE_POSITIVE -> samplesToCSV(map[group]?.cls?.truePositive?.samples)
            ClassificationType.FALSE_POSITIVE -> samplesToCSV(map[group]?.cls?.falsePositive?.samples)
            ClassificationType.FALSE_NEGATIVE -> samplesToCSV(map[group]?.cls?.falseNegative?.samples)
            ClassificationType.NO_MATCH -> samplesToCSV(map[group]?.cls?.noMatch?.samples)
        }
    }

    fun samplesToCsv(classType: ClassificationType): String {
        return when (classType) {
            ClassificationType.TRUE_POSITIVE -> samplesToCSV(classes.truePositive.samples)
            ClassificationType.FALSE_NEGATIVE -> samplesToCSV(classes.falseNegative.samples)
            ClassificationType.NO_MATCH -> samplesToCSV(classes.noMatch.samples)
            else -> ""
        }
    }


    private fun samplesToCSV(comps: List<TermComparison>?): String = samplesToCSV(comps, hypothesis, reference)

    override fun samplesToCSV(): String {
        throw NotImplementedError()
    }
}