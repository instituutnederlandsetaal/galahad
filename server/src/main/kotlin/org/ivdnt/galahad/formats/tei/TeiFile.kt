package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.annotations.LayerReader
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import java.io.BufferedInputStream
import java.io.File

class TeiFile(
    override val file: File,
    override val format: DocumentFormat,
) : InternalFile() {
    override val reader: LayerReader by lazy { TeiReader(BufferedInputStream(file.inputStream())) }
}