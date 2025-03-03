package org.ivdnt.galahad

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Weigher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.data.layer.Layer
import java.io.File
import kotlin.io.path.Path


val mapper: ObjectMapper by lazy { ObjectMapper() }

abstract class FileBackedCache<T>(
    file: File,
    initValue: T,
) : FileBackedValue<T>( file, initValue ) {

    abstract fun isValid( lastModified: Long ): Boolean // is cache valid?
    abstract fun set(): T

    inline fun <reified S : T>get(): T {
        return if( isValid( lastModified ) ) {
            read<S>()
        } else {
            // log
            logger.debug("Cache of type ${S::class.simpleName} is not valid. Will set new value.")
            // if not valid:
            val newValue = set()
            modify<S> { newValue }
            newValue
        }
    }

}

// the implementation of this class might be open for improvement,
// but it suffices for now.
// for examples see https://stackoverflow.com/questions/44589669/correctly-implementing-wait-and-notify-in-kotlin
open class FileBackedValue<T>(
    val file: File,
    val initValue: T, // required to avoid null
) : Logging {

    companion object {
        /** Special cache for [Layer] objects because of their large size */
        val cache: Cache<String, Layer> = Caffeine.newBuilder()
            .recordStats()
            .maximumWeight(100_000_000) // 100MB
            // Weigher is used once at put() time
            .weigher<String, Layer>(Weigher { key, value -> File(key).length().toInt() })
            .build()
    }

    init {
        file.parentFile.mkdirs()
    }

    val lastModified: Long
        get() = file.lastModified()

    inline fun <reified S : T>read(): T {
        // It was not set yet
        if( !file.exists() || file.length() == 0L ) {
            return initValue
        }

        // For [Layer]s, try getting from cache
        if (S::class == Layer::class) {
            cache.getIfPresent(file.absolutePath)?.let { return it as T }
        }

        // else read from disk
        val bytes: ByteArray = file.readBytes()
        val result = mapper.readValue(bytes, object : TypeReference<S>() {})
        // For [Layer]s, put in cache
        if (S::class == Layer::class) {
            cache.put(file.absolutePath, result as Layer)
        }
        return result
    }

    /**
     * modify can be used to set a new value based on the current value
     * in particular you can use it as a 'write' like:
     * modify<MyType> { newValue }
     * An example of modification:
     * modify<Int> { oldValue++ }
     */
    inline fun <reified S : T>modify( modification: (T) -> T ) {
        if(!file.exists()) {
            file.createNewFile()
        }
        // Would love to do this atomically, but for now we won't
        val oldValue = read<S>()
        val newValue = modification(oldValue)
        val newValBytes = mapper.writeValueAsBytes(newValue)
        runBlocking(Dispatchers.IO) { file.writeBytes(newValBytes) }
        // For [Layer]s, put in cache
        if (S::class == Layer::class) {
            cache.put(file.absolutePath, newValue as Layer)
        }
    }
}