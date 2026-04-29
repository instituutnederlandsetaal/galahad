package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.LayerReader
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import java.io.File

class ConlluFile(
    override val file: File
) : InternalFile() {
    override val format: DocumentFormat = DocumentFormat.Conllu
    override val reader: LayerReader by lazy { ConlluReader(file) }
}