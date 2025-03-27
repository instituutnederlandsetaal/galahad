package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import java.io.File

class TxtFile(
    override val file: File,
) : InternalFile() {
    override val format: DocumentFormat = DocumentFormat.Txt
    override val reader: TxtReader by lazy { TxtReader(file) }
}