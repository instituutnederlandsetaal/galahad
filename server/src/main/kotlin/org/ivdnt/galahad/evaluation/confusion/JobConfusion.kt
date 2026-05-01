package org.ivdnt.galahad.evaluation.confusion

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.DocumentEvaluations
import org.ivdnt.galahad.evaluation.EvaluationEntry
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.export.csv.CsvFile
import org.ivdnt.galahad.export.csv.CsvString
import org.ivdnt.galahad.util.merge

/**
 * Part of speech confusion of a corpus for two different tagger layers. A CorpusConfusion is the
 * sum of the [DocumentConfusion]s of all documents in the corpus.
 */
class JobConfusion(
    @JsonValue val confusion: Map<Annotation, Map<String, Map<String, EvaluationEntry>>>
) {
    companion object {
        fun create(corpus: Corpus, docEvals: DocumentEvaluations): JobConfusion =
            JobConfusion(
                corpus.documents
                    .readAllSequence()
                    .map { docEvals.createOrThrow(it.name).confusion.confusion }
                    .reduce { map1, map2 ->
                        map1.merge(map2) { v1, v2 ->
                            v1.merge(v2) { m1, m2 ->
                                m1.merge(m2) { e1, e2 ->
                                    EvaluationEntry.add(
                                        e1,
                                        e2,
                                        truncate = docEvals.jobs.filter != null,
                                    )
                                }
                            }
                        }
                    }
            )

        fun toCsv(confusion: Map<String, Map<String, EvaluationEntry>>): CsvString = buildString {
            val header = sortedHeader(confusion)
            append(CsvFile.toCsvString(listOf("Hypothesis → Reference ↓").plus(header)))
            sortedEntries(confusion).forEach { (group, entries) ->
                val row = mutableListOf(group)
                header.forEach { col -> row.add(entries[col]?.count?.toString() ?: "0") }
                append(CsvFile.toCsvString(row))
            }
        }

        private fun sortedHeader(
            confusion: Map<String, Map<String, EvaluationEntry>>
        ): List<String> {
            val sorted: MutableList<String> =
                confusion.values.flatMap { it.keys }.distinct().sorted().toMutableList()
            // Move MISSING_MATCH to last.
            if (sorted.remove(TermComparison.MISSING_MATCH)) { // true if it was present
                sorted.add(TermComparison.MISSING_MATCH) // so add it last
            }
            return sorted
        }

        private fun sortedEntries(
            confusion: Map<String, Map<String, EvaluationEntry>>
        ): List<Map.Entry<String, Map<String, EvaluationEntry>>> {
            val sorted = confusion.entries.sortedBy { it.key }.toMutableList()
            // Move MISSING_MATCH to last.
            val missing = sorted.firstOrNull { it.key == TermComparison.MISSING_MATCH }
            if (missing != null) {
                sorted.remove(missing)
                sorted.add(missing)
            }
            return sorted
        }
    }
}
