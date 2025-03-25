package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.export.csv.CSVFile
import org.ivdnt.galahad.taggers.Tagger

interface CsvSampleExporter {
    fun samplesToCSV(): String
    fun samplesToCSV(comps: List<TermComparison>?, hypoJob: Tagger, refJob: Tagger): String {
        var csv = ""

        // [Tagger].produces is a Set<>, making the order unpredictable
        // So create an alphabetically sorted list once and reuse it
        val hypoColumns = hypoJob.annotations.sorted()
        val refColumns = refJob.annotations.sorted()

        // header
        val columns: MutableList<String> = mutableListOf("token")
        columns.addAll(refColumns.map { "${refJob.id} ${it.value}" })
        columns.addAll(hypoColumns.map { "${hypoJob.id} ${it.value}" })
        csv += CSVFile.toCSVHeader(columns)

        // body
        comps?.forEach { termComp ->
            val literal = termComp.hypoTerm.token.ifEmpty { termComp.refTerm.token }
            val refAnnots = refColumns.map { termComp.refTerm.annotations[it] ?: Term.missingName(it) }
            val hypoAnnots = hypoColumns.map { termComp.hypoTerm.annotations[it] ?: Term.missingName(it) }
            csv += CSVFile.toCSVRecord(listOf(literal) + refAnnots + hypoAnnots)
        }
        return csv
    }
}