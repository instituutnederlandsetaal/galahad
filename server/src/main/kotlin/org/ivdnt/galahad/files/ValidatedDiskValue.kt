package org.ivdnt.galahad.files

import java.io.File

/**
 * [DiskValue] that is always valid due to being recreated when invalid. While valid, reads from
 * cache or disk.
 */
abstract class ValidatedDiskValue<T>(file: File) : DiskValue<T>(file) {
    /**
     * Whether the value is still valid. Should check whether any modifications were made after
     * [modified].
     *
     * @param modified The last modified time of the file.
     */
    abstract fun isValid(modified: Long): Boolean

    /** Set value to be written when invalid. */
    abstract fun set(): T

    /** Read from cache, disk, or create when invalid. */
    inline fun <reified T> readOrCreate(): T =
        if (isValid(modified)) {
            readOrThrow<T>()
        } else {
            write<T>(set() as T)
        }
}
