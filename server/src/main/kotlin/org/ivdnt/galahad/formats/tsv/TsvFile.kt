package org.ivdnt.galahad.formats.tsv

import java.io.File
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader

class TsvFile(override val file: File) : ParsedFile() {
    override val format: DocumentFormat = DocumentFormat.Tsv
    override val reader: LayerReader by lazy { TsvReader(file) }
}
