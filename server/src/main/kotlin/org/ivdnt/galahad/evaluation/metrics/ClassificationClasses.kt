package org.ivdnt.galahad.evaluation.metrics

import org.ivdnt.galahad.evaluation.comparison.EvaluationEntry

class ClassificationClasses(
    var truePositive: EvaluationEntry = EvaluationEntry(),
    var falsePositive: EvaluationEntry = EvaluationEntry(),
    var falseNegative: EvaluationEntry = EvaluationEntry(),
    var noMatch: EvaluationEntry = EvaluationEntry(),
) {
    val hypCount: Int
        get() = truePositive.count + falsePositive.count

    val refCount: Int
        get() = truePositive.count + falseNegative.count

    val metrics: ClassificationMetrics
        get() = ClassificationMetrics.from(this)

    fun classification(classification: String): EvaluationEntry {
        return when (classification) {
            "truePositive" -> truePositive
            "falsePositive" -> falsePositive
            "falseNegative" -> falseNegative
            "noMatch" -> noMatch
            else -> EvaluationEntry()
        }
    }

    fun add(other: ClassificationClasses, truncate: Boolean = true): ClassificationClasses {
        truePositive = EvaluationEntry.add(truePositive, other.truePositive, truncate)
        falsePositive = EvaluationEntry.add(falsePositive, other.falsePositive, truncate)
        falseNegative = EvaluationEntry.add(falseNegative, other.falseNegative, truncate)
        noMatch = EvaluationEntry.add(noMatch, other.noMatch, truncate)
        return this
    }

    operator fun plus(other: ClassificationClasses): ClassificationClasses =
        ClassificationClasses(
            truePositive = EvaluationEntry.from(other.truePositive, truePositive),
            falsePositive = EvaluationEntry.from(other.falsePositive, falsePositive),
            falseNegative = EvaluationEntry.from(other.falseNegative, falseNegative),
            noMatch = EvaluationEntry.from(other.noMatch, noMatch),
        )
}
