package org.ivdnt.galahad.export

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.exceptions.InvalidDocumentFormatException
import org.ivdnt.galahad.formats.conllu.ConlluMerger
import org.ivdnt.galahad.formats.folia.FoliaMerger
import org.ivdnt.galahad.formats.tei.TeiMerger
import org.ivdnt.galahad.formats.tsv.TsvMerger
import java.io.OutputStream

abstract class LayerMerger protected constructor(protected val export: DocumentExport) {
    protected val termComparisons: List<TermComparison> = LayerComparison(export).matches

    abstract fun merge(out: OutputStream)

    companion object {
        fun create(export: DocumentExport): LayerMerger = when (export.format) {
            DocumentFormat.Tsv -> TsvMerger(export)
            DocumentFormat.Folia -> FoliaMerger(export)
            DocumentFormat.Conllu -> ConlluMerger(export)
            DocumentFormat.TeiP5 -> TeiMerger(export)
            else -> throw InvalidDocumentFormatException("Unsupported merge format: ${export.format}")
        }
    }
}
