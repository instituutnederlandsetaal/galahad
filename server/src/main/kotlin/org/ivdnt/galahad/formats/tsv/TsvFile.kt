package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.annotations.LayerReader
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import java.io.File

class TsvFile(
    override val file: File,
) : InternalFile() {
    override val format: DocumentFormat = DocumentFormat.Tsv
    override val reader: LayerReader by lazy { TsvReader(file) }
}