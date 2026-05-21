package org.ivdnt.galahad.formats.conllu

import java.io.File
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader

class ConlluFile(override val file: File) : ParsedFile() {
    override val format: DocumentFormat = DocumentFormat.Conllu
    override val reader: LayerReader by lazy { ConlluReader(file) }
}
