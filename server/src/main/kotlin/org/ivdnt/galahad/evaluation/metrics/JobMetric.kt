package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.DocumentEvaluations
import org.ivdnt.galahad.evaluation.csv.CsvFile
import org.ivdnt.galahad.evaluation.csv.CsvString
import org.ivdnt.galahad.util.merge

class JobMetric(@JsonValue val classesByGroup: Map<String, NewMetric>) {

    fun toGlobalCsv(): CsvString = buildString {
        append(
            CsvFile.toCsvString(
                listOf(
                    "annotation",
                    "grouped by",
                    "macro precision",
                    "macro recall",
                    "macro f1",
                    "micro accuracy",
                    "count",
                    "true positive",
                    "false negative",
                    "no match",
                )
            )
        )
        for ((_, value) in classesByGroup) {
            append(
                CsvFile.toCsvString(
                    listOf(
                        value.settings.annotation,
                        value.settings.group,
                        value.macro.precision,
                        value.macro.recall,
                        value.macro.f1,
                        value.micro.accuracy, // todo other micros
                        value.classes.hypCount,
                        value.classes.truePositive.count,
                        value.classes.falseNegative.count,
                        value.classes.noMatch.count,
                    )
                )
            )
        }
    }

    companion object {
        fun create(
            corpus: Corpus,
            docEvals: DocumentEvaluations,
            annotation: Annotation,
            group: Annotation,
        ): JobMetric =
            JobMetric(
                corpus.documents
                    .readAllSequence()
                    .map {
                        docEvals.createOrThrow(it.name).getMetrics(annotation, group).classesByGroup
                    }
                    .reduce { map1, map2 ->
                        map1.merge(map2) { a, b ->
                            a.apply { grouped.merge(b.grouped) { x, y -> x.add(y) } }
                        } as MutableMap<String, NewMetric>
                    }
                    .mapValues { NewMetric(it.value.settings, it.value.grouped) }
            )

        fun toCsv(metric: NewMetric): CsvString = buildString {
            append(
                CsvFile.toCsvString(
                    listOf(
                        "group",
                        "precision",
                        "recall",
                        "f1",
                        "hypothesis count",
                        "reference count",
                        "true positive",
                        "false positive",
                        "false negative",
                        "no match",
                    )
                )
            )
            for ((key, value) in metric.grouped) {
                append(
                    CsvFile.toCsvString(
                        listOf(
                            key,
                            value.metrics.precision,
                            value.metrics.recall,
                            value.metrics.f1,
                            value.hypCount,
                            value.refCount,
                            value.truePositive.count,
                            value.falsePositive.count,
                            value.falseNegative.count,
                            value.noMatch.count,
                        )
                    )
                )
            }
        }
    }
}
