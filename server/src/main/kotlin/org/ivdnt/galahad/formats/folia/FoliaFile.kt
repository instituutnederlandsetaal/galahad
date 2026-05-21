package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader
import java.io.BufferedInputStream
import java.io.File

class FoliaFile(override val file: File) : ParsedFile() {
    override val format: DocumentFormat = DocumentFormat.Folia
    override val reader: LayerReader by lazy {
        FoliaReader(BufferedInputStream(file.inputStream()))
    }
}
