package org.ivdnt.galahad.files

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Weigher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Layer
import java.io.File

open class DiskValue<T>(
    val file: File,
) : Logging {
    val lastModified: Long
        get() = file.lastModified()

    inline fun <reified T> readOrNull(): T? {
        if (file.length() == 0L) return null

        // For [Layer]s, try getting from cache
        if (T::class == Layer::class) {
            cache.getIfPresent(file.absolutePath)?.let { return it as T }
        }

        // else read from disk
        val bytes: ByteArray = file.readBytes()
        val result = mapper.readValue(bytes, object : TypeReference<T>() {})
        // For [Layer]s, put in cache
        if (T::class == Layer::class) {
            cache.put(file.absolutePath, result as Layer)
        }
        return result
    }

    inline fun <reified T> readOrThrow(): T = readOrNull() ?: throw IllegalStateException("$file is missing or empty.")

    inline fun <reified T> write(value: T): T {
        val bytes = mapper.writeValueAsBytes(value)
        runBlocking(Dispatchers.IO) { file.writeBytes(bytes) }
        // For [Layer]s, put in cache
        if (T::class == Layer::class) {
            cache.put(file.absolutePath, value as Layer)
        }
        return value
    }

    companion object {
        val mapper = ObjectMapper()
        // TODO use cache for all DiskValues types
        /** Special cache for [Layer] objects because of their large size */
        val cache: Cache<String, Layer> = Caffeine.newBuilder().recordStats().maximumWeight(100_000_000) // 100MB
            // Weigher is used once at put() time
            .weigher<String, Layer>(Weigher { key, value -> File(key).length().toInt() }).build()
    }
}