package org.ivdnt.galahad.files

import org.apache.logging.log4j.kotlin.logger
import java.io.File

/**
 * Generic base class for file system operations.
 * Create, read and delete GalahadFiles in a folder.
 * Usage:
 * ```
 * val folder = GalahadFolder<ReadType, CreateType>(...)
 * val data: CreateType = ...
 * val key: String = ...
 *
 * val file = folder.createOrThrow(data)
 * val files = folder.readAll()
 *
 * folder.deleteOrNull(key)
 * if (folder.deleteOrNull(key) == null) { println("Nothing to delete") } // prints
 * // folder.deleteOrThrow(key) // throws
 *
 * val file2 = folder.readOrNull(key) // returns null
 * // val file3 = folder.readOrThrow(key) // throws
 */
abstract class GalahadFolderManager<ReadType : GalahadFolder, CreateType : Any>(
    dir: File,
) : GalahadFolder(dir) {

    protected abstract fun ctor(key: String): ReadType

    protected abstract fun throwNotFound(key: String): Nothing

    abstract fun createOrThrow(key: CreateType): ReadType

    open fun readAll(): Set<ReadType> = dir.list()?.map { readOrThrow(it) }?.toSet() ?: setOf()

    open fun readOrNull(key: String): ReadType? = if (dir.resolve(key).exists()) ctor(key) else null

    fun readOrThrow(key: String): ReadType = readOrNull(key) ?: throwNotFound(key)

    fun deleteOrThrow(key: String) {
        readOrThrow(key) // does it exist?
        if (!dir.resolve(key).deleteRecursively()) {
            logger.warn("Partial deletion of $key")
        }
    }

    fun deleteOrNull(key: String): Unit? = readOrNull(key)?.let { deleteOrThrow(key) }
}