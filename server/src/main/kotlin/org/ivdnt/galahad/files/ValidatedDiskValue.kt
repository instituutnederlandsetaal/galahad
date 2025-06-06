package org.ivdnt.galahad.files

import java.io.File

abstract class ValidatedDiskValue<T>(
    file: File,
) : DiskValue<T>(file) {
    abstract fun isValid(modified: Long): Boolean
    abstract fun set(): T

    inline fun <reified T> readOrCreate(): T = if (isValid(modified)) {
        readOrThrow<T>()
    } else {
        write<T>(set() as T)
    }
}