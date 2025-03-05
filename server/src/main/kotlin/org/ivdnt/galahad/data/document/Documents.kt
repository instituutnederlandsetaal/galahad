package org.ivdnt.galahad.data.document

import org.ivdnt.galahad.filesystem.GalahadFile
import org.ivdnt.galahad.app.CRDSet
import org.ivdnt.galahad.exceptions.DocumentNotFoundException
import java.io.File
import org.apache.logging.log4j.kotlin.logger

/**
 * Used as a collection for all documents in a corpus and to create and delete new documents.
 * Documents are saved as folders with their file name as folder name.
 * This class represents their common parent directory "documents/".
 */
class Documents(
    dir: File,
) : GalahadFile(dir), CRDSet<String, Document, File> {
    /**
     * Create a new document, which includes creating a directory,
     * storing the uploaded file, metadata, format, parsing it to plaintext and extracting source annotations.
     */
    override fun createOrThrow(file: File): Document {
        return Document.create(dir.resolve(file.name), file)
    }

    // Note: this is a relatively expensive operation, you might want to use a different method
    override fun readAll(): Set<Document> = dir.list()?.map { readOrThrow(it) }?.toSet() ?: setOf()

    /** Retrieve a single document */
    override fun readOrNull(key: String) =
        if (dir.resolve(key).exists()) Document(dir.resolve(key)) else null

    override fun readOrThrow(key: String) = readOrNull(key) ?: throw DocumentNotFoundException(key)

    /** Delete a single document */
    override fun deleteOrThrow(key: String) {
        readOrThrow(key) // does it exist?
        if (!dir.resolve(key).deleteRecursively()) {
            logger.warn("Partial deletion of $key")
        }
    }
}