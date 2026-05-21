package org.ivdnt.galahad.layers

import java.io.File
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.LayerNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager

class CorpusLayers(dir: File, private val corpus: Corpus) :
    GalahadFolderManager<CorpusLayer, File>(dir) {
    override fun ctor(key: String): CorpusLayer = CorpusLayer(dir.resolve(key), corpus)

    override fun throwNotFound(key: String): Nothing = throw LayerNotFoundException(key)
}
