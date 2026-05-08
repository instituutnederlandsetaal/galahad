package org.ivdnt.galahad.layers

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import java.io.File

class CorpusLayers(dir: File, private val corpus: Corpus) : GalahadFolderManager<CorpusLayer, File>(dir) {
    override fun ctor(key: String): CorpusLayer = CorpusLayer(dir.resolve(key), corpus)

    override fun throwNotFound(key: String): Nothing =
        throw JobNotFoundException(key) // TODO layer not found
}
