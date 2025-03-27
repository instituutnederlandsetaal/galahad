package org.ivdnt.galahad.export

import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.InvalidDocumentFormatException
import org.ivdnt.galahad.formats.conllu.ConlluConverter
import org.ivdnt.galahad.formats.folia.FoliaConverter
import org.ivdnt.galahad.formats.naf.NafConverter
import org.ivdnt.galahad.formats.tei.TeiConverter
import org.ivdnt.galahad.formats.tsv.TsvConverter
import org.ivdnt.galahad.formats.txt.TxtConverter
import java.io.OutputStream

abstract class LayerConverter(protected val export: DocumentExport) {
    abstract fun convert(out: OutputStream)

    companion object {
        fun create(export: DocumentExport): LayerConverter = when (export.format) {
            DocumentFormat.Tsv -> TsvConverter(export)
            DocumentFormat.Folia -> FoliaConverter(export)
            DocumentFormat.Naf -> NafConverter(export)
            DocumentFormat.Txt -> TxtConverter(export)
            DocumentFormat.Conllu -> ConlluConverter(export)
            DocumentFormat.TeiP5 -> TeiConverter(export)
            else -> throw InvalidDocumentFormatException("Unsupported export conversion format: ${export.format}")
        }
    }
}