package org.ivdnt.galahad.formats.txt

import java.io.OutputStream
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerWriter

class TxtWriter(export: DocumentExport) : LayerWriter(export) {
    override fun convert(out: OutputStream): Unit = out.write(export.layer.toString().toByteArray())
}
