package org.ivdnt.galahad.export

import org.ivdnt.galahad.annotations.Term
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
    private val layerComparison: LayerComparison = LayerComparison(export)
    // TODO move to LayerComparison
    protected val sourceTermComparisons: List<TermComparison> =
        (layerComparison.matches + layerComparison.referenceTermsWithoutMatches.map {
            TermComparison(
                Term.EMPTY,
                it
            )
        }).sortedBy { it.refTerm.offset }

    abstract fun merge(out: OutputStream)

    companion object {
        fun create(export: DocumentExport): LayerMerger = when (export.format) {
            DocumentFormat.Tsv -> TsvMerger(export)
            DocumentFormat.Folia -> FoliaMerger(export)
            DocumentFormat.Conllu -> ConlluMerger(export)
            DocumentFormat.TeiP5Legacy, DocumentFormat.TeiP5 -> TeiMerger(export)
            else -> throw InvalidDocumentFormatException("Unsupported export conversion format: ${export.format}")
        }
    }
}
