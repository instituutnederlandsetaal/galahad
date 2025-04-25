package org.ivdnt.galahad.formats.naf

import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import java.io.File

class NafFile(
    override val file: File,
) : InternalFile() {
    override val format: DocumentFormat = DocumentFormat.Naf
    override val reader: AnnotationReader by lazy { NafReader(file) }
}
