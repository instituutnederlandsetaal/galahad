package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import java.io.File

class FoliaFile(
    override val file: File,
) : InternalFile() {
    override val format: DocumentFormat = DocumentFormat.Folia
    override val reader: FoliaReader by lazy { FoliaReader(file) }
}