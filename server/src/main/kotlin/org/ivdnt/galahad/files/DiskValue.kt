package org.ivdnt.galahad.files

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.ivdnt.galahad.files.DiskValue.Companion.cache
import org.ivdnt.galahad.util.JsonUtil

/**
 * Wrapper around serializable value stored on disk to retain between sessions. Implements a cache.
 */
open class DiskValue<T>(val file: File) {
    val modified: Long
        get() = if (file.exists()) Files.getLastModifiedTime(file.toPath()).toMillis() else 0L

    /** Try to read value from [cache] or disk, else return null. */
    inline fun <reified T> readOrNull(): T? {
        // First try cache
        cache.getIfPresent(file.absolutePath)?.let {
            return it as T
        }
        // Else read from disk
        if (file.length() == 0L) return null // Not on disk
        return JsonUtil.mapper
            .readValue(file, T::class.java)
            // And cache it
            .also { cache.put(file.absolutePath, it as Any) }
    }

    /** Try to read value from [cache] or disk, else throw [IllegalStateException]. */
    inline fun <reified T> readOrThrow(): T =
        readOrNull() ?: throw IllegalStateException("$file is missing or empty.")

    /** Write value to disk and cache it. */
    inline fun <reified T> write(value: T): T {
        val bytes = JsonUtil.mapper.writeValueAsBytes(value)
        // Write to file as blocking.
        runBlocking(Dispatchers.IO) { file.writeBytes(bytes) }
        // Cache it.
        cache.put(file.absolutePath, value as Any)
        return value
    }

    companion object {
        /** Global cache for all values that are written to or read from disk at some point. */
        val cache: Cache<String, Any> =
            Caffeine.newBuilder()
                .recordStats()
                .maximumWeight(500_000_000) // 500MB
                // Weigher is used once at put() time. Weigh based on file size.
                .weigher<String, Any> { key, _ -> File(key).length().toInt() }
                .build()
    }
}
