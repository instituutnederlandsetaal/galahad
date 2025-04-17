package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.InternalFile
import java.io.BufferedInputStream
import java.io.File

class TeiFile(
    override val file: File,
    override val format: DocumentFormat,
) : InternalFile() {
    override val reader: AaltoTeiReader by lazy { AaltoTeiReader(BufferedInputStream(file.inputStream())) }
    constructor(file: File) : this(file, DocumentFormat.fromFile(file))
}