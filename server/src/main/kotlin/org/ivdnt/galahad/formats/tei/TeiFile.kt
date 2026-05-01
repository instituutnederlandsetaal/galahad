package org.ivdnt.galahad.formats.tei

import java.io.BufferedInputStream
import java.io.File
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader

class TeiFile(override val file: File, override val format: DocumentFormat) : ParsedFile() {
    override val reader: LayerReader by lazy { TeiReader(BufferedInputStream(file.inputStream())) }
}
