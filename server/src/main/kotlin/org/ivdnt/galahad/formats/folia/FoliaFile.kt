package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import java.io.BufferedInputStream
import java.io.File

class FoliaFile(
    override val file: File,
) : InternalFile() {
    override val format: DocumentFormat = DocumentFormat.Folia
    override val reader: AnnotationReader by lazy { AaltoFoliaReader(BufferedInputStream(file.inputStream())) }
}