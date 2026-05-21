package org.ivdnt.galahad.formats.json

import java.io.OutputStream
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerWriter
import org.ivdnt.galahad.util.JsonUtil

class JsonWriter(export: DocumentExport) : LayerWriter(export) {
    override fun convert(out: OutputStream): Unit =
        out.write(JsonUtil.prettyMapper.writeValueAsBytes(export.layer))
}
