package org.ivdnt.galahad.files

import java.io.File

abstract class GalahadFolder(
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
