package org.ivdnt.galahad.files

import java.io.File
import java.nio.file.Files

abstract class GalahadFolder(protected val dir: File) {
    // Note the Kotlin initialization order. Make the dir before access.
    init {
        dir.mkdirs()
    }

    val name: String = dir.name
    val modified: Long
        get() =
            dir.listFiles().maxOfOrNull { Files.getLastModifiedTime(it.toPath()).toMillis() }
                ?: dir.lastModified()

    val size: Long
        get() = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}
