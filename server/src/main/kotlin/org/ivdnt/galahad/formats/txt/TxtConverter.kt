package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import java.io.OutputStream

class TxtConverter(export: DocumentExport) : LayerConverter(export) {
    override fun convert(out: OutputStream): Unit = out.write(export.layer.toString().toByteArray())
}