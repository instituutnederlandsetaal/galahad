package org.ivdnt.galahad.formats.plain

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.exceptions.MergeNotImplementedException
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.InternalFile
import java.io.File
import java.io.Reader

class PlainFile(
    override val file: File,
) : InternalFile {
    override val format: DocumentFormat = DocumentFormat.Txt

    override fun plainTextReader(): Reader {
        return file.reader()
    }

    override fun merge(transformMetadata: DocumentTransformMetadata): PlainFile {
        // merging does not make sense for PlainFile
        throw MergeNotImplementedException(format.identifier)
    }

    override fun sourceLayer(): Layer = Layer.EMPTY.apply { name = SOURCE_LAYER_NAME }
}