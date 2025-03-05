package org.ivdnt.galahad.filesystem

import org.apache.logging.log4j.kotlin.logger
import java.io.File


abstract class GalahadFile(
    open val dir: File,
) {
    init {
        dir.mkdirs()
    }

    val name: String = dir.name

    val lastModified: Long
        get() = dir.walkTopDown().map { it.lastModified() }.reduceOrNull { f, g -> f.coerceAtLeast(g) } ?: -1

    val sizeInBytes: Long
        get() = dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
}

// TODO: decide whether to use this
abstract class GalahadFolder<ReadType : GalahadFile, CreateType>(
    override val dir: File,
) : GalahadFile(dir) {
    inline fun <reified ReadType> readAll(): Set<ReadType> = dir.list()?.map { readOrThrow<ReadType>(it) }?.toSet() ?: setOf()
    inline fun <reified ReadType> readOrThrow(key: String): ReadType = readOrNull(key) ?: throw Exception("Failed to read $key")
    inline fun <reified ReadType> readOrNull(key: String): ReadType? {
        return if (dir.resolve(key).exists()) {
            ReadType::class.java.getConstructor(File::class.java).newInstance(dir.resolve(key)) as ReadType
        } else {
            null
        }
    }
    inline fun <reified ReadType> delete(key: String) {
        readOrThrow<ReadType>(key) // does it exist?
        if (!dir.resolve(key).deleteRecursively()) {
            logger.warn("Partial deletion of $key")
        }
    }
    abstract fun createOrThrow(key: CreateType): ReadType
}