package org.ivdnt.galahad.formats.plain

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.exceptions.MergeNotImplementedException
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.formats.PlainTextableFile
import java.io.File
import java.io.Reader

class PlainFile(
    override val file: File,
) : InternalFile, PlainTextableFile {
    override val format: DocumentFormat = DocumentFormat.Txt

    override fun plainTextReader(): Reader {
        return file.reader()
    }

    override fun merge(transformMetadata: DocumentTransformMetadata): PlainFile {
        // merging does not make sense for PlainFile
        throw MergeNotImplementedException(format.identifier)
    }
}