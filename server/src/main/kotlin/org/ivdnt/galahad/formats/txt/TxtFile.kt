package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader
import java.io.File

class TxtFile(override val file: File) : ParsedFile() {
    override val format: DocumentFormat = DocumentFormat.Txt
    override val reader: LayerReader by lazy { TxtReader(file) }
}
