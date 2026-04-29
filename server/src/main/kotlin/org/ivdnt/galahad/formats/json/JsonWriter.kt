package org.ivdnt.galahad.formats.json

import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerWriter
import org.ivdnt.galahad.util.JsonUtil
import java.io.OutputStream

class JsonWriter(export: DocumentExport) : LayerWriter(export) {
    override fun convert(out: OutputStream): Unit = out.write(JsonUtil.prettyMapper.writeValueAsBytes(export.layer))
}