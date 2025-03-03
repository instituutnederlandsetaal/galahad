package org.ivdnt.galahad.formats.xml

import org.ivdnt.galahad.formats.PlainTextableFile
import org.ivdnt.galahad.formats.SourceLayerableFile
import java.io.File

abstract class AnnotatedFile (
        file: File
) : XMLFile( file ), PlainTextableFile, SourceLayerableFile
