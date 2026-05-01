package org.ivdnt.galahad.util

import java.io.File
import org.ivdnt.galahad.documents.DocumentFormat

val File.withoutFormatExt: String
    get() {
        DocumentFormat.extensions.forEach {
            if (this.name.endsWith(it)) {
                return this.name.substringBeforeLast(it)
            }
        }
        return this.nameWithoutExtension
    }
