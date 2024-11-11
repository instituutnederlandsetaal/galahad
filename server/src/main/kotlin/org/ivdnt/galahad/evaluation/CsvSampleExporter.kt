package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.port.csv.CSVFile

interface CsvSampleExporter {
   fun samplesToCSV(): String
   fun samplesToCSV(comps: List<TermComparison>?): String {
      var csv = ""
      // header
      val columns: MutableList<String> = mutableListOf("literal")
      comps?.firstOrNull()?.refTerm?.annotations?.keys?.forEach {
         columns.add("reference ${it.value}")
      }
        comps?.firstOrNull()?.hypoTerm?.annotations?.keys?.forEach {
             columns.add("hypothesis ${it.value}")
        }
      csv += CSVFile.toCSVHeader(columns)

      // body
      comps?.forEach { termComp ->
         var literal = termComp.hypoTerm.literals
         if (literal.isEmpty()) {
            literal = termComp.refTerm.literals
         }
         val refAnnots = termComp.refTerm.annotations.map { (key, value) -> value ?: Term.missingName(key) }
         val hypoAnnots = termComp.hypoTerm.annotations.map { (key, value) -> value ?: Term.missingName(key) }
         csv += CSVFile.toCSVRecord(listOf(literal) + refAnnots + hypoAnnots)
      }
        return csv
   }
}