package org.ivdnt.galahad.files

import java.io.File

abstract class GalahadFolder(open val dir: File) {
    // Note the Kotlin initialization order. Make the dir before access.
    init {
        dir.mkdirs()
    }

    val name: String = dir.name
    val modified: Long
        get() = dir.listFiles().maxOfOrNull { it.lastModified() } ?: -1

    val size: Long
        get() = dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
}
