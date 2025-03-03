package org.ivdnt.galahad.data.document

import org.ivdnt.galahad.BaseFileSystemStore
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
    workDirectory: File,
) : BaseFileSystemStore(workDirectory), CRDSet<String, Document, File> {

    val allNames: List<String>
        get() = workDirectory.list()?.toList() ?: throw Exception("Could not read document names")

    override fun readOrThrow(key: String): Document {
        return readOrNull(key) ?: throw DocumentNotFoundException(key)
    }

    /** Retrieve a single document */
    override fun readOrNull(key: String) =
        if (workDirectory.resolve(key).exists()) Document(workDirectory.resolve(key)) else null

    // Note: this is a relatively expensive operation, you might want to use a different method
    override fun readAll(): Set<Document> = workDirectory.listFiles()?.map { Document(it) }?.toSet() ?: setOf()

    /** Delete a single document */
    override fun delete(key: String) {
        val fullyDeleted: Boolean = workDirectory.resolve(key).deleteRecursively()
        if (!fullyDeleted) logger.warn("Partial deletion of $key")
    }

    /**
     * Create a new document, which includes creating a directory,
     * storing the uploaded file, metadata, format, parsing it to plaintext and extracting source annotations.
     */
    override fun createOrNull(file: File): Document? {
        return Document.create(workDirectory.resolve(file.name), file)
    }
}