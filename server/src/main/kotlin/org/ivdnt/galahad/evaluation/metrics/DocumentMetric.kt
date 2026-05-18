package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.EvaluationEntry
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.TermComparison

class ClsMetrics(
    val precision: Float = 0f,
    val recall: Float = 0f,
    val f1: Float = 0f,
    // could also include count. Simply TP + FN + MM
) {
    operator fun plus(other: ClsMetrics): ClsMetrics =
        ClsMetrics(precision + other.precision, recall + other.recall, f1 + other.f1)

    operator fun div(divisor: Int): ClsMetrics = this * (1.0f / divisor)

    operator fun times(factor: Float): ClsMetrics =
        ClsMetrics(precision * factor, recall * factor, f1 * factor)
}

class FlatMetricsSettings(val id: String, val annotation: String, val group: String) {
    constructor(settings: MetricsSettings) : this(settings.id, settings.annotation, settings.group)
}

class NewMetric(
    val settings: FlatMetricsSettings,
    val grouped: MutableMap<String, ClsClasses> = mutableMapOf(),
    val classes: ClsClasses =
        grouped.filter { it.key != TermComparison.MISSING_MATCH }.values.reduce { a, b -> a + b },
    val accuracy: Float = classes.truePositive.count / classes.count.toFloat(),
    val macro: ClsMetrics =
        grouped.values.map { it.metrics }.reduce { a, b -> a + b } / grouped.size,
)

class ClsClasses(
    var truePositive: EvaluationEntry = EvaluationEntry(),
    var falsePositive: EvaluationEntry = EvaluationEntry(),
    var falseNegative: EvaluationEntry = EvaluationEntry(),
    var noMatch: EvaluationEntry = EvaluationEntry(),
    var count: Int = truePositive.count + falseNegative.count + noMatch.count,
    var metrics: ClsMetrics = toMetrics(truePositive, falsePositive, falseNegative, noMatch),
) {
    fun add(other: ClsClasses, truncate: Boolean = true): ClsClasses {
        truePositive = EvaluationEntry.add(truePositive, other.truePositive, truncate)
        falsePositive = EvaluationEntry.add(falsePositive, other.falsePositive, truncate)
        falseNegative = EvaluationEntry.add(falseNegative, other.falseNegative, truncate)
        noMatch = EvaluationEntry.add(noMatch, other.noMatch, truncate)
        count = truePositive.count + falseNegative.count + noMatch.count
        metrics = toMetrics(truePositive, falsePositive, falseNegative, noMatch)
        return this
    }

    operator fun plus(other: ClsClasses): ClsClasses =
        ClsClasses(
            truePositive = EvaluationEntry.from(other.truePositive, truePositive),
            falsePositive = EvaluationEntry.from(other.falsePositive, falsePositive),
            falseNegative = EvaluationEntry.from(other.falseNegative, falseNegative),
            noMatch = EvaluationEntry.from(other.noMatch, noMatch),
            count = truePositive.count + falseNegative.count + noMatch.count,
            metrics = toMetrics(truePositive, falsePositive, falseNegative, noMatch),
        )

    companion object {
        fun notNaN(value: Float): Float = if (value.isNaN()) 0.0f else value

        fun toMetrics(
            truePositive: EvaluationEntry,
            falsePositive: EvaluationEntry,
            falseNegative: EvaluationEntry,
            noMatch: EvaluationEntry,
        ): ClsMetrics {
            val tp = truePositive.count.toFloat()
            val fp = falsePositive.count.toFloat()
            val fn = falseNegative.count.toFloat()
            val mm = noMatch.count.toFloat()
            val precision = notNaN(tp / (tp + fp))
            val recall = notNaN(tp / (tp + fn + mm))
            val f1 = notNaN(2.0f * (precision * recall) / (precision + recall))
            return ClsMetrics(precision, recall, f1)
        }
    }
}

class DocumentMetric(@JsonValue val classesByGroup: MutableMap<String, NewMetric>) {
    companion object {
        fun create(layerComparison: LayerComparison, annotations: Set<Annotation>): DocumentMetric =
            DocumentMetric(
                buildMap<String, MutableMap<String, ClsClasses>> {
                        layerComparison.matches.forEach { tc ->
                            METRIC_TYPES.filter { annotations.containsAll(it.requiredAnnotations) }
                                .forEach { metricType ->
                                    if (!metricType.filterBy(tc)) return@forEach
                                    val mapsToAdd = mutableListOf<MutableMap<String, ClsClasses>>()
                                    if (tc.hyp == Term.EMPTY) {
                                        // handle missing match
                                        val cls =
                                            ClsClasses(
                                                noMatch = EvaluationEntry(1, mutableListOf(tc))
                                            )
                                        mapsToAdd.add(
                                            mutableMapOf(metricType.groupBy(tc.ref) to cls)
                                        )
                                    } else {
                                        // handle true positive & false negative
                                        val (trueEntry, falseEntry) =
                                            truesFalses(tc, metricType::termsEqual)
                                        val cls =
                                            ClsClasses(
                                                truePositive = trueEntry,
                                                falseNegative = falseEntry,
                                            )
                                        val group = metricType.groupBy(tc.ref)
                                        val classesMap = mutableMapOf(group to cls)
                                        mapsToAdd.add(classesMap)
                                        // handle false positive
                                        if (falseEntry.count > 0) {
                                            val cls = ClsClasses(falsePositive = falseEntry)
                                            val group = metricType.groupBy(tc.hyp)
                                            val classesMap = mutableMapOf(group to cls)
                                            mapsToAdd.add(classesMap)
                                        }
                                    }
                                    for (map in mapsToAdd) {

                                        merge(metricType.id, map) { a, b ->
                                            a.apply {
                                                this.merge(b.keys.first(), b.values.first()) { x, y
                                                    ->
                                                    x.add(y)
                                                }
                                            }
                                        }
                                    }
                                }
                        }
                    }
                    .mapValues {
                        NewMetric(
                            FlatMetricsSettings(METRIC_TYPES.first { mt -> mt.id == it.key }),
                            it.value,
                        )
                    }
                    .toMutableMap()
            )

        private fun truesFalses(
            comp: TermComparison,
            cond: (TermComparison) -> Boolean,
        ): Pair<EvaluationEntry, EvaluationEntry> {
            val trues =
                if (cond(comp)) {
                    EvaluationEntry(1, mutableListOf(comp))
                } else {
                    EvaluationEntry()
                }
            val falses =
                if (!cond(comp)) {
                    EvaluationEntry(1, mutableListOf(comp))
                } else {
                    EvaluationEntry()
                }
            return Pair(trues, falses)
        }
    }
}
