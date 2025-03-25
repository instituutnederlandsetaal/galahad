package org.ivdnt.galahad.export

import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.InvalidDocumentFormatException
import org.ivdnt.galahad.formats.conllu.ConlluMerger
import org.ivdnt.galahad.formats.folia.export.FoliaMerger
import org.ivdnt.galahad.formats.tsv.TsvMerger
import java.io.File
import java.io.OutputStream

abstract class LayerMerger(protected val export: DocumentExport) {

    abstract fun merge(out: OutputStream): File

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