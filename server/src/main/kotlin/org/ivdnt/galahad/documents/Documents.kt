package org.ivdnt.galahad.documents

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.DocumentNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import java.io.File

/**
 * Create, read and delete documents in a corpus.
 * Represents the "documents/" folder in a corpus folder.
 * Usage:
 * ```
 * val documents = corpus.documents // some existing corpus
 * val file = File("annotations.tsv")
 * val key = file.name
 *
 * val doc = documents.createOrThrow(file)
 * val all = documents.readAll()
 *
 * documents.deleteOrNull(key)
 * if (documents.deleteOrNull(key) == null) { println("Nothing to delete") } // prints
 * // documents.deleteOrThrow(key) // throws
 *
 * val doc2 = documents.readOrNull(key) // returns null
 * // val doc3 = documents.readOrThrow(key) // throws
 * ```
 */
class Documents(
    dir: File,
    val corpus: Corpus,
) : GalahadFolderManager<Document, File>(dir) {
    override fun createOrThrow(key: File): Document = Document.create(dir.resolve(key.name), key, corpus)
    override fun ctor(key: String): Document = Document(dir.resolve(key))
    override fun throwNotFound(key: String): Nothing = throw DocumentNotFoundException(key)
}