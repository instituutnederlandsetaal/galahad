package org.ivdnt.galahad.util

import org.ivdnt.galahad.documents.DocumentFormat
import java.io.File

val File.withoutFormatExt: String
    get() {
        DocumentFormat.extensions.forEach {
            if (this.name.endsWith(it)) {
                return this.name.substringBeforeLast(it)
            }
        }
        return this.nameWithoutExtension
    }

fun File.asFormat(format: DocumentFormat): String = this.withoutFormatExt + "." + format.extension
