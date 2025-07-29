package org.ivdnt.galahad.files

import com.fasterxml.jackson.core.type.TypeReference
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.ivdnt.galahad.util.JsonUtil
import org.ivdnt.galahad.util.ThreadPoolUtil
import java.io.File

/** Wrapper around serializable value stored on disk to retain between sessions. Implements a cache. */
open class DiskValue<T>(
    val file: File,
) {
    val modified: Long get() = file.lastModified()

    /** Try to read value from cache or disk, else return null. */
    inline fun <reified T> readOrNull(): T? {
        // First try cache
        cache.getIfPresent(file.absolutePath)?.let { return it as T }
        // Else read from disk
        if (file.length() == 0L) return null // Just to be safe
        return JsonUtil.mapper.readValue(file.readBytes(), object : TypeReference<T>() {})
            // And cache it later, return now.
            .also { ThreadPoolUtil.pool.execute { cache.put(file.absolutePath, it as Any) } }
    }

    /** Try to read value from cache or disk, else throw exception. */
    inline fun <reified T> readOrThrow(): T = readOrNull() ?: throw IllegalStateException("$file is missing or empty.")

    /** Write value to disk and cache it. */
    inline fun <reified T> write(value: T): T {
        val bytes = JsonUtil.mapper.writeValueAsBytes(value)
        // Write to file as blocking.
        runBlocking(Dispatchers.IO) { file.writeBytes(bytes) }
        // Cache it later and return now.
        ThreadPoolUtil.pool.execute {
            cache.put(file.absolutePath, value as Any)
        }
        return value
    }

    companion object {
        /** Global cache for all values that are written to or read from disk at some point. */
        val cache: Cache<String, Any> = Caffeine.newBuilder().recordStats().maximumWeight(500_000_000) // 500MB
            // Weigher is used once at put() time. Weigh based on file size.
            .weigher<String, Any> { key, _ -> File(key).length().toInt() }.build()
    }
}

