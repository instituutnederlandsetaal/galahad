package org.ivdnt.galahad.formats.xml

import org.ivdnt.galahad.formats.InternalFile
import java.io.File

abstract class XMLFile(
    final override val file: File,
) : InternalFile