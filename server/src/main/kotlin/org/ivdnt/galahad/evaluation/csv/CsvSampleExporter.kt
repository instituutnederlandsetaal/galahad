package org.ivdnt.galahad.evaluation.csv

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.layers.CorpusLayer

class CsvSampleExporter {
    companion object {

        fun samplesToCSV(
            comps: List<TermComparison>?,
            hypothesis: CorpusLayer,
            reference: CorpusLayer,
        ): String {
            var csv = ""

            // [Tagger].produces is a Set<>, making the order unpredictable
            // So create an alphabetically sorted list once and reuse it

            val hypoMeta = hypothesis.metadata
            val hypoColumns = hypoMeta.annotations.keys - Annotation.TOKEN
            val refMeta = reference.metadata
            val refColumns = refMeta.annotations.keys - Annotation.TOKEN

            // header
            val columns: MutableList<String> = mutableListOf("token")
            columns.addAll(refColumns.map { "${refMeta.tagger.name} ${it.value}" })
            columns.addAll(hypoColumns.map { "${hypoMeta.tagger.name} ${it.value}" })
            csv += CsvFile.toCsvString(columns)

            // body
            comps?.forEach { termComp ->
                val literal = termComp.hyp.token.ifEmpty { termComp.ref.token }
                val refAnnots = refColumns.map {
                    termComp.ref.annotations[it] ?: Term.missingName(it)
                }
                val hypoAnnots = hypoColumns.map {
                    termComp.hyp.annotations[it] ?: Term.missingName(it)
                }
                csv += CsvFile.toCsvString(listOf(literal) + refAnnots + hypoAnnots)
            }
            return csv
        }
    }
}
