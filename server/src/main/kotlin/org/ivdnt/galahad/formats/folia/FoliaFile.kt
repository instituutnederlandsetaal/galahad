package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.formats.folia.export.FoliaLayerMerger
import java.io.File

class FoliaFile(
    override val file: File,
) : InternalFile {
    override val format: DocumentFormat = DocumentFormat.Folia
    private var isParsed: Boolean = false
    private var reader: FoliaReader? = null

    override fun merge(transformMetadata: DocumentTransformMetadata): FoliaFile {
        return FoliaLayerMerger(this, transformMetadata).merge()
    }

    private fun parse() {
        reader = FoliaReader(file) { _, _, _ -> }
        reader?.read()
        isParsed = true
    }

    override fun plainText(): String {
        if (!isParsed) parse()
        // TODO: make this an efficient implementation
        return reader!!.plainTextBuilder.toString()
    }

    override fun sourceLayer(): Layer {
        if (!isParsed) parse()
        return reader!!.sourceLayer
    }
}