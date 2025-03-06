package org.ivdnt.galahad.data.document

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.exceptions.DocumentNotFoundException
import org.ivdnt.galahad.filesystem.GalahadFileManager
import java.io.File

/**
 * Used as a collection for all documents in a corpus and to create and delete new documents.
 * Documents are saved as folders with their file name as folder name.
 * This class represents their common parent directory "documents/".
 */
class Documents(
    dir: File,
    val corpus: Corpus,
) : GalahadFileManager<Document, File>(dir) {
    override fun createOrThrow(key: File) = Document.create(dir.resolve(key.name), key, corpus)
    override fun ctor(key: String) = Document(dir.resolve(key))
    override fun throwNotFound(key: String) = throw DocumentNotFoundException(key)
}