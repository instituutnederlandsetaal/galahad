package org.ivdnt.galahad.formats

import org.ivdnt.galahad.app.ExpensiveGettable
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.formats.conllu.ConlluFile
import org.ivdnt.galahad.formats.folia.FoliaFile
import org.ivdnt.galahad.formats.naf.NAFFile
import org.ivdnt.galahad.formats.plain.PlainFile
import org.ivdnt.galahad.formats.tei.TEIFile
import org.ivdnt.galahad.formats.tsv.TSVFile
import java.io.File
import java.io.Reader

/** A document parsed as a file of a certain file type, e.g. TEI, TSV, Folia. */
interface InternalFile {

    val documentName: String
        get() = file.nameWithoutExtension

    val file: File

    val format: DocumentFormat

    /**
     * merge the uploaded file with the data from the layer, creating a new file
     */
    fun merge( transformMetadata: DocumentTransformMetadata): InternalFile

    fun sourceLayer(): Layer
    fun plainTextReader(): Reader

    companion object {
        fun from(file: File, format: DocumentFormat): ExpensiveGettable<InternalFile> = object : ExpensiveGettable<InternalFile> {
            override fun expensiveGet(): InternalFile {
                return when (format) {
                    DocumentFormat.Tsv -> TSVFile(file)
                    DocumentFormat.TeiP4Legacy,
                    DocumentFormat.TeiP5Legacy,
                    DocumentFormat.TeiP5 -> TEIFile(file, format)
                    DocumentFormat.Folia -> FoliaFile(file)
                    DocumentFormat.Naf -> NAFFile(file)
                    DocumentFormat.Txt -> PlainFile(file)
                    DocumentFormat.Conllu -> ConlluFile(file)
                    else -> throw DocumentInvalidException(file.name, "Unsupported format.")
                }
            }
        }
    }

}

