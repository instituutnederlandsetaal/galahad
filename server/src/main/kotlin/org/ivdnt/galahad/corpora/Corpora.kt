package org.ivdnt.galahad.corpora

import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import java.io.File

/**
 * Create, read and delete corpora.
 * Represents a corpora collection folder on the file system.
 * Usage:
 * ```
 * val corpora = Corpora(File("corpora/"))
 * val meta = MutableCorpusMetadata(...)
 * val key = meta.id.toString()
 *
 * val corpus = folder.createOrThrow(meta)
 * val all = folder.readAll()
 *
 * val newMeta = MutableCorpusMetadata(...)
 * val updatedCorpus = folder.updateOrThrow(newMeta)
 *
 * folder.deleteOrNull(key)
 * if (folder.deleteOrNull(key) == null) { println("Nothing to delete") } // prints
 * // folder.deleteOrThrow(key) // throws
 *
 * val corpus2 = folder.readOrNull(key) // returns null
 * // val corpus3 = folder.readOrThrow(key) // throws
 *
 */
class Corpora(
    dir: File,
) : GalahadFolderManager<Corpus, MutableCorpusMetadata>(dir) {
    override fun createOrThrow(key: MutableCorpusMetadata): Corpus = Corpus.create(dir.resolve(key.id.toString()), key)
    override fun ctor(key: String): Corpus = Corpus(dir.resolve(key))
    override fun throwNotFound(key: String): Nothing = throw CorpusNotFoundException(key)

    fun updateOrThrow(newMeta: MutableCorpusMetadata): Corpus {
        val corpus = readOrThrow(newMeta.id.toString())
        val oldMeta = corpus.mutableMetadata
        val cleanMetadata = MutableCorpusMetadata.clean(newMeta, oldMeta)
        corpus.mutableMetadata = cleanMetadata
        return corpus
    }
}