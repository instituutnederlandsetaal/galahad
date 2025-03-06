package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.exceptions.MergeNotImplementedException
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.InternalFile
import java.io.File

class TxtFile(
    override val file: File,
) : InternalFile {
    override val format: DocumentFormat = DocumentFormat.Txt
    override val plaintext: String by lazy { file.readText() }
    override val sourceLayer: Layer = Layer.EMPTY.apply { name = SOURCE_LAYER_NAME }

    override fun merge(transformMetadata: DocumentTransformMetadata): TxtFile {
        // merging does not make sense for PlainFile
        throw MergeNotImplementedException(format.identifier)
    }
}