package org.ivdnt.galahad.formats

import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.formats.conllu.ConlluFile
import org.ivdnt.galahad.formats.folia.FoliaFile
import org.ivdnt.galahad.formats.naf.NafFile
import org.ivdnt.galahad.formats.tei.TeiFile
import org.ivdnt.galahad.formats.tsv.TsvFile
import org.ivdnt.galahad.formats.txt.TxtFile
import java.io.File

/** A document parsed as a file of a certain file type, e.g. TEI, TSV, Folia. */
abstract class InternalFile {
    abstract val file: File
    abstract val format: DocumentFormat
    val plaintext: String by lazy { reader.layer.toString() }
    val layer: Layer by lazy { reader.layer }
    protected abstract val reader: AnnotationReader

    companion object {
        fun create(file: File): InternalFile {
            return when (val format = DocumentFormat.fromFile(file)) {
                DocumentFormat.Tsv -> TsvFile(file)
                DocumentFormat.Folia -> FoliaFile(file)
                DocumentFormat.Naf -> NafFile(file)
                DocumentFormat.Txt -> TxtFile(file)
                DocumentFormat.Conllu -> ConlluFile(file)
                // Multiple TEI formats
                DocumentFormat.TeiP4Legacy,
                DocumentFormat.TeiP5Legacy,
                DocumentFormat.TeiP5,
                    -> TeiFile(file, format)

                else -> throw DocumentInvalidException(file.name, "Unsupported format.")
            }
        }
    }
}

