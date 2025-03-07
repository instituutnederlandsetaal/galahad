package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.formats.folia.export.FoliaLayerMerger
import java.io.File

class FoliaFile(
    override val file: File,
) : InternalFile {
    override val format: DocumentFormat = DocumentFormat.Folia
    override val plaintext: String by lazy { parse(); reader!!.plainTextBuilder.toString() }
    override val sourceLayer: Layer by lazy { parse(); reader!!.sourceLayer }

    private var isParsed: Boolean = false
    private var reader: FoliaReader? = null

    override fun merge(transformMetadata: DocumentTransformMetadata): FoliaFile =
        FoliaLayerMerger(this, transformMetadata).merge()

    private fun parse() {
        if (isParsed) return // Don't double parse
        isParsed = true

        reader = FoliaReader(file) { _, _, _ -> }
        reader?.read()
    }
}