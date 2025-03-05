package org.ivdnt.galahad.formats

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.document.FormatInducer
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.formats.conllu.ConlluFile
import org.ivdnt.galahad.formats.folia.FoliaFile
import org.ivdnt.galahad.formats.naf.NAFFile
import org.ivdnt.galahad.formats.plain.PlainFile
import org.ivdnt.galahad.formats.tei.TEIFile
import org.ivdnt.galahad.formats.tsv.TSVFile
import java.io.File

/** A document parsed as a file of a certain file type, e.g. TEI, TSV, Folia. */
interface InternalFile {
    val file: File
    val format: DocumentFormat
    val plaintext: String
    val sourceLayer: Layer

    /**
     * merge the uploaded file with the data from the layer, creating a new file.
     */
    fun merge(transformMetadata: DocumentTransformMetadata): InternalFile

    companion object {
        fun create(file: File): InternalFile {
            return when (val format = FormatInducer.determineFormat(file)) {
                DocumentFormat.Tsv -> TSVFile(file)
                DocumentFormat.Folia -> FoliaFile(file)
                DocumentFormat.Naf -> NAFFile(file)
                DocumentFormat.Txt -> PlainFile(file)
                DocumentFormat.Conllu -> ConlluFile(file)
                // Multiple TEI formats
                DocumentFormat.TeiP4Legacy,
                DocumentFormat.TeiP5Legacy,
                DocumentFormat.TeiP5,
                    -> TEIFile(file, format)

                else -> throw DocumentInvalidException(file.name, "Unsupported format.")
            }
        }
    }
}

