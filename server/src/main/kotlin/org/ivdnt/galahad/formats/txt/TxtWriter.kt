package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerWriter
import java.io.OutputStream

class TxtWriter(export: DocumentExport) : LayerWriter(export) {
    override fun convert(out: OutputStream): Unit = out.write(export.layer.toString().toByteArray())
}