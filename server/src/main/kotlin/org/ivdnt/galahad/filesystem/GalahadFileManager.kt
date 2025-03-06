package org.ivdnt.galahad.filesystem

import org.apache.logging.log4j.kotlin.logger
import java.io.File

abstract class GalahadFileManager<ReadType : GalahadFile, CreateType: Any>(
    dir: File,
) : GalahadFile(dir) {

    abstract fun createOrThrow(key: CreateType): ReadType

    protected abstract fun ctor(key: String): ReadType

    protected abstract fun throwNotFound(key: String): Nothing

    open fun readAll(): Set<ReadType> = dir.list()?.map { readOrThrow(it) }?.toSet() ?: setOf()

    open fun readOrNull(key: String) = if (dir.resolve(key).exists()) ctor(key) else null

    fun readOrThrow(key: String) = readOrNull(key) ?: throwNotFound(key)

    fun deleteOrThrow(key: String) {
        readOrThrow(key) // does it exist?
        if (!dir.resolve(key).deleteRecursively()) {
            logger.warn("Partial deletion of $key")
        }
    }

    fun deleteOrNull(key: String) = readOrNull(key)?.let { deleteOrThrow(key) }
}