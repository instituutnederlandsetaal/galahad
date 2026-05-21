package org.ivdnt.galahad.formats.txt

import java.io.File
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader

class TxtFile(override val file: File) : ParsedFile() {
    override val format: DocumentFormat = DocumentFormat.Txt
    override val reader: LayerReader by lazy { TxtReader(file) }
}
