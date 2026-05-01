package org.ivdnt.galahad.formats.folia

import java.io.OutputStream
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerMerger

class FoliaMerger(export: DocumentExport) : LayerMerger(export) {

    override fun merge(out: OutputStream) {}
}
