package org.ivdnt.galahad.files

import java.io.File

abstract class ValidatedDiskValue<T>(
    file: File,
) : DiskValue<T>(file) {
    abstract fun isValid(lastModified: Long): Boolean
    abstract fun set(): T

    inline fun <reified T> readOrCreate(): T = if (isValid(lastModified)) {
        readOrThrow<T>()
    } else {
        logger.debug("DiskValue<${T::class.simpleName}> is invalid. Will set new value.")
        write<T>(set() as T)
    }
}