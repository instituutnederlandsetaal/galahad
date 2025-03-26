package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.annotations.*
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.formats.conllu.ConlluFile
import java.io.File
import java.io.FileOutputStream

open class TsvFile(
    override val file: File,
) : InternalFile() {
    override val format: DocumentFormat = DocumentFormat.Tsv
    override val reader: TsvReader by lazy { TsvReader(file) }
}