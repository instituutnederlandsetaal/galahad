package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader
import java.io.File

class ConlluFile(override val file: File) : ParsedFile() {
    override val format: DocumentFormat = DocumentFormat.Conllu
    override val reader: LayerReader by lazy { ConlluReader(file) }
}
