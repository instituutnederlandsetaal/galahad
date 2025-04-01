package org.ivdnt.galahad.files

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.ivdnt.galahad.util.ThreadPoolUtil
import java.io.File


open class DiskValue<T>(
    val file: File,
) {
    val lastModified: Long get() = file.lastModified()

    inline fun <reified T> readOrNull(): T? {
        if (file.length() == 0L) return null

        cache.getIfPresent(file.absolutePath)?.let { return it as T }

        return mapper.readValue(file.readBytes(), object : TypeReference<T>() {})
            .also { ThreadPoolUtil.pool.execute { cache.put(file.absolutePath, it as Any) } }
    }

    inline fun <reified T> readOrThrow(): T = readOrNull() ?: throw IllegalStateException("$file is missing or empty.")

    inline fun <reified T> write(value: T): T {
        val bytes = mapper.writeValueAsBytes(value)
        runBlocking(Dispatchers.IO) { file.writeBytes(bytes) }
        ThreadPoolUtil.pool.execute {
            cache.put(file.absolutePath, value as Any)
        }
        return value
    }

    companion object {
        val mapper: ObjectMapper = ObjectMapper().apply {
            registerKotlinModule()
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
        val cache: Cache<String, Any> = Caffeine.newBuilder().recordStats().maximumWeight(100_000_000) // 100MB
            // Weigher is used once at put() time
            .weigher<String, Any> { key, _ -> File(key).length().toInt() }.build()
    }
}

