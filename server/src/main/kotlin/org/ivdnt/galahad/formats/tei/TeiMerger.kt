package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerMerger
import java.io.OutputStream


class TeiMerger(
    export: DocumentExport,
) : LayerMerger(export) {
    override fun merge(out: OutputStream) {

    }
}