package org.ivdnt.galahad.formats.naf

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader
import java.io.File

class NafFile(override val file: File) : ParsedFile() {
    override val format: DocumentFormat = DocumentFormat.Naf
    override val reader: LayerReader by lazy { NafReader(file) }
}
