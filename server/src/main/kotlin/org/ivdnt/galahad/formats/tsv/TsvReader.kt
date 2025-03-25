package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.AnnotationReader
import java.io.File

class TsvReader(
    file: File
): AnnotationReader(file) {
    override fun read(): Layer = Layer(listOf())
}