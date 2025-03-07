package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.DocumentExport
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.formats.folia.export.FoliaLayerMerger
import java.io.File

class FoliaFile(
    override val file: File,
) : InternalFile {
    override val format: DocumentFormat = DocumentFormat.Folia
    override val plaintext: String by lazy { reader.plainTextBuilder.toString() }
    override val sourceLayer: Layer by lazy { reader.sourceLayer }
    private val reader: FoliaReader by lazy { FoliaReader(file) { _, _, _ -> }.also { it.read() } }
    override fun merge(export: DocumentExport): FoliaFile = FoliaLayerMerger(this, export).merge()
}