package org.ivdnt.galahad.layers

import java.io.File
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager

class CorpusLayers(dir: File, val corpus: Corpus) : GalahadFolderManager<CorpusLayer, File>(dir) {
    override fun ctor(key: String): CorpusLayer = CorpusLayer(dir.resolve(key))

    override fun throwNotFound(key: String): Nothing =
        throw JobNotFoundException(key) // TODO layer not found
}
