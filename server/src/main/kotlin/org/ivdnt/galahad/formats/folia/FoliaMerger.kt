package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerMerger
import java.io.OutputStream

class FoliaMerger(export: DocumentExport) : LayerMerger(export) {

    override fun merge(out: OutputStream) {}
}
