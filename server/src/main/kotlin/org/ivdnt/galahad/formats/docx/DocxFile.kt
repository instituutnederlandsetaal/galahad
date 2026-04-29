package org.ivdnt.galahad.formats.docx

import org.ivdnt.galahad.annotations.LayerReader
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import java.io.BufferedInputStream
import java.io.File

class DocxFile(
    override val file: File
) : InternalFile() {
    override val format: DocumentFormat = DocumentFormat.Docx
    override val reader: LayerReader by lazy { DocxReader(BufferedInputStream(file.inputStream())) }
}