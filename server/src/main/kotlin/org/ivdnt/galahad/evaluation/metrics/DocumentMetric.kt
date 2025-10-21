package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.EvaluationEntry
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.TermComparison

class ClsMetrics(
    val precision: Float = 0f,
    val recall: Float = 0f,
    val f1: Float = 0f,
    // could also include count. Simply TP + FN + MM
) {
    operator fun plus(other: ClsMetrics): ClsMetrics = ClsMetrics(
        precision + other.precision,
        recall + other.recall,
        f1 + other.f1,
    )

    operator fun div(divisor: Int): ClsMetrics = this * (1.0f / divisor)

    operator fun times(factor: Float): ClsMetrics = ClsMetrics(
        precision * factor,
        recall * factor,
        f1 * factor,
    )
}

class NewMetric(
    val grouped: MutableMap<String, ClsClasses> = mutableMapOf(),
) {
    val classes: ClsClasses
        get() = grouped.filter { it.key != TermComparison.MISSING_MATCH }.values.reduce { a, b -> a + b }

    val micro: ClsMetrics get() = classes.metrics

    val macro: ClsMetrics
        get() {
            val validClasses = grouped.filter { it.key != TermComparison.MISSING_MATCH }
            return validClasses.values.map { it.metrics }
                .reduce { a, b -> a + b } / validClasses.size
        }
}

class ClsClasses(
    var truePositive: EvaluationEntry = EvaluationEntry(),
    var falsePositive: EvaluationEntry = EvaluationEntry(),
    var falseNegative: EvaluationEntry = EvaluationEntry(),
    var noMatch: EvaluationEntry = EvaluationEntry(),
) {
    fun add(other: ClsClasses, truncate: Boolean = true): ClsClasses {
        truePositive = EvaluationEntry.add(truePositive, other.truePositive, truncate)
        falsePositive = EvaluationEntry.add(falsePositive, other.falsePositive, truncate)
        falseNegative = EvaluationEntry.add(falseNegative, other.falseNegative, truncate)
        noMatch = EvaluationEntry.add(noMatch, other.noMatch, truncate)
        return this
    }

    operator fun plus(other: ClsClasses): ClsClasses = ClsClasses(
        truePositive = EvaluationEntry.from(other.truePositive,truePositive),
        falsePositive = EvaluationEntry.from(other.falsePositive,falsePositive),
        falseNegative = EvaluationEntry.from(other.falseNegative,falseNegative),
        noMatch = EvaluationEntry.from(other.noMatch,noMatch),
    )

    val metrics: ClsMetrics
        get() {
            val precision = notNaN(truePositive.count / (truePositive.count + falsePositive.count).toFloat())
            val recall = notNaN(truePositive.count / (truePositive.count + falsePositive.count + noMatch.count).toFloat())
            val f1 = notNaN(2.0f * (precision * recall) / (precision + recall))
            return ClsMetrics(precision, recall, f1)
        }

    val count: Int
        get() = truePositive.count + falseNegative.count + noMatch.count

    companion object {
        fun notNaN(value: Float): Float = if (value.isNaN()) 0.0f else value
    }
}


class DocumentMetric(
    @JsonValue
    val classesByGroup: MutableMap<String, NewMetric>
) {
    companion object {
        fun create(layerComparison: LayerComparison): DocumentMetric = DocumentMetric(
            buildMap<String, NewMetric> {
                layerComparison.matches.forEach { tc ->
                    METRIC_TYPES.forEach { metricType ->
                        if (!metricType.filterBy(tc)) return@forEach
                        val mapsToAdd = mutableListOf<MutableMap<String, ClsClasses>>()
                        if (tc.hyp == Term.EMPTY) {
                            // handle missing match
                            val cls = ClsClasses(noMatch = EvaluationEntry(1, mutableListOf(tc)))
                            val group = TermComparison.MISSING_MATCH
                            val classesMap = mutableMapOf(group to cls)
                            mapsToAdd.add(classesMap)
                        } else {
                            // handle true positive & false negative
                            val (trueEntry, falseEntry) = truesFalses(tc, metricType::termsEqual)
                            val cls = ClsClasses(truePositive = trueEntry, falseNegative = falseEntry)
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

                            merge(metricType.id, NewMetric(grouped=map)) { oldMetricMap, newMetricMap -> oldMetricMap.apply {
                                this.grouped.merge(newMetricMap.grouped.keys.first(), newMetricMap.grouped.values.first()) { oldCls, newCls ->
                                    oldCls.add(newCls)
                                }
                            }
                            }
                        }
                    }
                }
            }.toMutableMap()
        )

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
    }
}