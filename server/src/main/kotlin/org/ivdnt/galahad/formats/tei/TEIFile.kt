package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import java.io.File

class TeiFile(
    override val file: File,
    override val format: DocumentFormat,
) : InternalFile() {
    override val reader: TeiReader by lazy { TeiReader(file) }
    constructor(file: File) : this(file, DocumentFormat.fromFile(file))
}