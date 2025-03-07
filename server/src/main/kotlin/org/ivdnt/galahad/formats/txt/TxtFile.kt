package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.MergeNotImplementedException
import org.ivdnt.galahad.formats.DocumentExport
import org.ivdnt.galahad.formats.InternalFile
import java.io.File

class TxtFile(
    override val file: File,
) : InternalFile {
    override val format: DocumentFormat = DocumentFormat.Txt
    override val plaintext: String by lazy { reader.layer.toString() }
    override val sourceLayer: Layer = Layer.EMPTY.apply { name = SOURCE_LAYER_NAME }
    private val reader: TxtReader by lazy { TxtReader(file) }

    override fun merge(export: DocumentExport): TxtFile {
        // merging does not make sense for PlainFile
        throw MergeNotImplementedException(format.identifier)
    }
}