package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.formats.csv.CSVFile
import org.ivdnt.galahad.taggers.Tagger

interface CsvSampleExporter {
    fun samplesToCSV(): String
    fun samplesToCSV(comps: List<TermComparison>?, hypoJob: Tagger, refJob: Tagger): String {
        var csv = ""

        // [Tagger].produces is a Set<>, making the order unpredictable
        // So create an alphabetically sorted list once and reuse it
        val hypoColumns = hypoJob.annotationTypes.sorted()
        val refColumns = refJob.annotationTypes.sorted()

        // header
        val columns: MutableList<String> = mutableListOf("token")
        columns.addAll(refColumns.map { "${refJob.id} ${it.value}" })
        columns.addAll(hypoColumns.map { "${hypoJob.id} ${it.value}" })
        csv += CSVFile.toCSVHeader(columns)

        // body
        comps?.forEach { termComp ->
            val literal = termComp.hypoTerm.literals.ifEmpty { termComp.refTerm.literals }
            val refAnnots = refColumns.map { termComp.refTerm.annotations[it] ?: Term.missingName(it) }
            val hypoAnnots = hypoColumns.map { termComp.hypoTerm.annotations[it] ?: Term.missingName(it) }
            csv += CSVFile.toCSVRecord(listOf(literal) + refAnnots + hypoAnnots)
        }
        return csv
    }
}