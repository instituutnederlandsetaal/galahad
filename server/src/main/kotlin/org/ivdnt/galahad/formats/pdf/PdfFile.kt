package org.ivdnt.galahad.formats.pdf

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.formats.docx.DocxReader
import java.io.BufferedInputStream
import java.io.File

class PdfFile(
    override val file: File
) : InternalFile() {
    override val format: DocumentFormat = DocumentFormat.Pdf
    override val reader: PdfReader by lazy { PdfReader(BufferedInputStream(file.inputStream())) }
}