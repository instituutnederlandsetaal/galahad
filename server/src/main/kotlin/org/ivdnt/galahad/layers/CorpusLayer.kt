package org.ivdnt.galahad.layers

import java.io.File
import org.ivdnt.galahad.documents.Documents
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue

class CorpusLayer(dir: File) : GalahadFolder(dir) {
    val documents: Documents = Documents(dir.resolve(DOCUMENTS_FOLDER))
    val metadata: CorpusLayerMetadata
        get() =
            object : ValidatedDiskValue<CorpusLayerMetadata>(dir.resolve(METADATA_FILE)) {
                    override fun isValid(modified: Long) = modified >= this@CorpusLayer.modified

                    override fun set(): CorpusLayerMetadata =
                        CorpusLayerMetadata.create(this@CorpusLayer)
                }
                .readOrCreate()

    companion object {
        private const val METADATA_FILE = "metadata.json"
        private const val DOCUMENTS_FOLDER = "documents"
    }
}
