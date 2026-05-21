package org.ivdnt.galahad.formats.docx

import java.io.BufferedInputStream
import java.io.File
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader

class DocxFile(override val file: File) : ParsedFile() {
    override val format: DocumentFormat = DocumentFormat.Docx
    override val reader: LayerReader by lazy { DocxReader(BufferedInputStream(file.inputStream())) }
}
