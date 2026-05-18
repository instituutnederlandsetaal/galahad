package org.ivdnt.galahad.formats.pdf

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader
import java.io.BufferedInputStream
import java.io.File

class PdfFile(override val file: File) : ParsedFile() {
    override val format: DocumentFormat = DocumentFormat.Pdf
    override val reader: LayerReader by lazy { PdfReader(BufferedInputStream(file.inputStream())) }
}
