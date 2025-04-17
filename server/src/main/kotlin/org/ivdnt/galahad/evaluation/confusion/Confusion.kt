package org.ivdnt.galahad.evaluation.confusion

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.evaluation.EvaluationEntry
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.export.csv.CSVFile

const val MULTIPLE_POS: String = "MULTIPLE"
const val OTHER_POS: String = "OTHER"
const val OTHER_POS_REGEX: String = """^[^a-zA-Z]"""

/**
 * Generic class for the part of speech confusion of a corpus or document.
 * The idea is to sum up the confusions as we go through the terms one by one using [add].
 */
open class Confusion(private val truncate: Boolean = true, val annotation: Annotation) {

    /**
     * null-key if there is no match
     */
    @JsonIgnore
    val matrix: MutableMap<Pair<String, String>, EvaluationEntry> = HashMap()

    /**
     * Confusions as a table, created from [matrix].
     * The first dimension represents the rows (hypothesis layer), the second dimension the columns (reference layer).
     */
    val table: Map<String, Map<String, EvaluationEntry>>
        get() {
            val ret = HashMap<String, Map<String, EvaluationEntry>>()
            matrix.forEach { (pair, evaluationEntry) ->
                ret.merge(
                    pair.second, mapOf(Pair(pair.first, evaluationEntry))
                ) { map1, map2 ->
                    (map1.asSequence() + map2.asSequence()).distinct()
                        .groupBy({ it.key }, { it.value })
                        .mapValues { (_, values) -> values.reduce { a, b -> EvaluationEntry.add(a, b) } }
                }
            }
            return ret
        }

    private fun getHeaderPossen(): List<String> {
        val sorted: MutableList<String> = table.values.flatMap { it.keys }.distinct().sorted().toMutableList()
        // Move MISSING_MATCH to last.
        if (sorted.remove(TermComparison.MISSING_MATCH)) { // returns true if element was present
            sorted.add(TermComparison.MISSING_MATCH)
        }
        return sorted
    }

    private fun getSortedTableEntries(): List<Map.Entry<String, Map<String, EvaluationEntry>>> {
        val sorted = table.entries.sortedBy { it.key }.toMutableList()
        // Move MISSING_MATCH to last.
        val missing = sorted.firstOrNull { it.key == TermComparison.MISSING_MATCH }
        if (missing != null) {
            sorted.remove(missing)
            sorted.add(missing)
        }
        return sorted
    }

    fun countsToCSV(): String {
        if (table.values.isEmpty()) return ""
        var ret = CSVFile.toCSVHeader(mutableListOf("Reference (down), hypothesis (across)") + getHeaderPossen())
        getSortedTableEntries().map {
            CSVFile.toCSVRecord(mutableListOf(it.key) + getHeaderPossen().map { h ->
                it.value[h]?.count ?: 0
            }.map { it.toString() })
        }.forEach { ret += it }
        return ret
    }

    // Cumulative addition functions

    fun add(termComp: TermComparison) {
        add(
            termComp.hypoTerm.annotationHeadOrMissing(annotation),
            termComp.refTerm.annotationHeadOrMissing(annotation),
            termComp
        )
    }

    fun add(other: Confusion) {
        other.matrix.forEach { (pair, evaluationEntry) ->
            add(pair.first, pair.second, evaluationEntry)
        }
    }

    fun add(hypoPos: String, refPos: String, sample: TermComparison) {
        add(hypoPos, refPos, 1, listOf(sample))
    }

    private fun add(pos1: String, pos2: String, count: Int, samples: List<TermComparison>) {
        add(pos1, pos2, EvaluationEntry(count, samples.toMutableList()))
    }

    // Base addition function called by the others
    private fun add(pos1: String, pos2: String, evaluationEntry: EvaluationEntry) {
        when {
            // Complex pos are mapped to a single category
            '+' in pos1 -> add(MULTIPLE_POS, pos2, evaluationEntry)
            '+' in pos2 -> add(pos1, MULTIPLE_POS, evaluationEntry)
            // Non-alphabetical pos are mapped to a single category "other"
            Regex(OTHER_POS_REGEX) in pos1 -> add(OTHER_POS, pos2, evaluationEntry)
            Regex(OTHER_POS_REGEX) in pos2 -> add(pos1, OTHER_POS, evaluationEntry)
            // Otherwise a simple merge
            else -> matrix.merge(Pair(pos1, pos2), evaluationEntry) { a, b -> EvaluationEntry.add(a, b, truncate) }
        }
    }
}