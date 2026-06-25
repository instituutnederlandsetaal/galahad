package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.EvaluationEntry
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.TermComparison

class ClsMetrics(
    val accuracy: Float = 0f,
    val precision: Float = 0f,
    val recall: Float = 0f,
    val f1: Float = 0f,
    // could also include count. Simply TP + FN + MM
) {
    operator fun plus(other: ClsMetrics): ClsMetrics =
        ClsMetrics(
            accuracy + other.accuracy,
            precision + other.precision,
            recall + other.recall,
            f1 + other.f1,
        )

    operator fun div(divisor: Int): ClsMetrics = this * (1.0f / divisor)

    operator fun times(factor: Float): ClsMetrics =
        ClsMetrics(accuracy * factor, precision * factor, recall * factor, f1 * factor)
}

class FlatMetricsSettings(val annotation: Annotation, val group: Annotation) {
    val name: String = "${annotation.value}-${group.value}"
}

class NewMetric(
    val settings: FlatMetricsSettings,
    val grouped: MutableMap<String, ClsClasses> = mutableMapOf(),
    val classes: ClsClasses =
        grouped.filter { it.key != TermComparison.MISSING_MATCH }.values.reduce { a, b -> a + b },
    val micro: ClsMetrics = classes.metrics,
    val macro: ClsMetrics =
        grouped.values.map { it.metrics }.reduce { a, b -> a + b } / grouped.size,
)

class ClsClasses(
    var truePositive: EvaluationEntry = EvaluationEntry(),
    var falsePositive: EvaluationEntry = EvaluationEntry(),
    var falseNegative: EvaluationEntry = EvaluationEntry(),
    var noMatch: EvaluationEntry = EvaluationEntry(),
    var hypCount: Int = truePositive.count + falsePositive.count,
    var refCount: Int = truePositive.count + falseNegative.count,
    // Should this not be a getter?
    var metrics: ClsMetrics = toMetrics(truePositive, falsePositive, falseNegative, noMatch),
) {
    fun add(other: ClsClasses, truncate: Boolean = true): ClsClasses {
        truePositive = EvaluationEntry.add(truePositive, other.truePositive, truncate)
        falsePositive = EvaluationEntry.add(falsePositive, other.falsePositive, truncate)
        falseNegative = EvaluationEntry.add(falseNegative, other.falseNegative, truncate)
        noMatch = EvaluationEntry.add(noMatch, other.noMatch, truncate)
        hypCount = truePositive.count + falsePositive.count
        refCount = truePositive.count + falseNegative.count
        metrics = toMetrics(truePositive, falsePositive, falseNegative, noMatch)
        return this
    }

    operator fun plus(other: ClsClasses): ClsClasses =
        ClsClasses(
            truePositive = EvaluationEntry.from(other.truePositive, truePositive),
            falsePositive = EvaluationEntry.from(other.falsePositive, falsePositive),
            falseNegative = EvaluationEntry.from(other.falseNegative, falseNegative),
            noMatch = EvaluationEntry.from(other.noMatch, noMatch),
            hypCount = truePositive.count + falsePositive.count,
            refCount = truePositive.count + falseNegative.count,
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
            val accuracy = notNaN(tp / (tp + fp + fn))
            val precision = notNaN(tp / (tp + fp))
            val recall = notNaN(tp / (tp + fn))
            val f1 = notNaN(2.0f * (precision * recall) / (precision + recall))
            return ClsMetrics(accuracy, precision, recall, f1)
        }
    }
}

class DocumentMetric(@JsonValue val classesByGroup: MutableMap<String, NewMetric>) {
    companion object {

        fun create(
            layerComparison: LayerComparison,
            //            annotations: List<Annotation>,
            annotation: Annotation,
            group: Annotation,
        ): DocumentMetric =
            DocumentMetric(
                buildMap<String, MutableMap<String, ClsClasses>> {
                        layerComparison.matches.forEach { tc ->
                            // if (!metricType.filterBy(tc)) return@forEach // was used for
                            // filtering on e.g. multi pos
                            val mapsToAdd = mutableListOf<MutableMap<String, ClsClasses>>()
                            if (tc.hyp == Term.EMPTY) {
                                // handle missing match
                                val cls =
                                    ClsClasses(noMatch = EvaluationEntry(1, mutableListOf(tc)))
                                mapsToAdd.add(
                                    mutableMapOf(tc.ref.annotationHeadOrMissing(group) to cls)
                                )
                            } else {
                                // handle true positive & false negative
                                var (trueEntry, falseEntry) =
                                    truesFalses(tc) { it.equal(annotation) }

                                if (trueEntry.count > 0) {
                                    val cls = ClsClasses(truePositive = trueEntry)
                                    val groupTP = tc.hyp.annotationHeadOrMissing(group)
                                    val classesMap = mutableMapOf(groupTP to cls)
                                    mapsToAdd.add(classesMap)
                                }

                                if (falseEntry.count > 0) {
                                    // false negative
                                    val clsFN = ClsClasses(falseNegative = falseEntry)
                                    val groupFN = tc.ref.annotationHeadOrMissing(group)
                                    val classesMapFN = mutableMapOf(groupFN to clsFN)
                                    mapsToAdd.add(classesMapFN)
                                    // handle false positive
                                    // copy
                                    val falseEntry2 =
                                        EvaluationEntry(
                                            falseEntry.count,
                                            falseEntry.samples.toMutableList(),
                                        )
                                    val cls = ClsClasses(falsePositive = falseEntry2)
                                    val groupFP = tc.hyp.annotationHeadOrMissing(group)
                                    val classesMap = mutableMapOf(groupFP to cls)
                                    mapsToAdd.add(classesMap)
                                }
                                // handle false negative based on group
                                //                                if (trueEntry.count > 0) {
                                //                                    val hypGroup =
                                // tc.hyp.annotationHeadOrMissing(group)
                                //                                    val refGroup =
                                // tc.ref.annotationHeadOrMissing(group)
                                //                                    if (hypGroup != refGroup) {
                                //                                        val clsFN =
                                // ClsClasses(falseNegative = trueEntry2)
                                //                                        val classesMap =
                                // mutableMapOf(refGroup to clsFN)
                                //                                        mapsToAdd.add(classesMap)
                                //                                    }
                                //                                }
                            }
                            for (map in mapsToAdd) {
                                merge("$annotation-$group", map) { a, b ->
                                    a.apply {
                                        this.merge(b.keys.first(), b.values.first()) { x, y ->
                                            x.add(y)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .mapValues {
                        NewMetric(
                            FlatMetricsSettings(annotation, group),
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
