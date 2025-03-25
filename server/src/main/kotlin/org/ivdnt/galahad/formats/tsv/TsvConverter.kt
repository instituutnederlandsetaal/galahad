package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import java.io.OutputStream

class TsvConverter(export: DocumentExport) : LayerConverter(export) {

    override fun convert(out: OutputStream) {
        // Header
        out.write("word\tlemma\tpos\n".encodeToByteArray()) // 'word' is the blacklab default
        // Body
        // We only write sentence boundaries (\n) and no #-comments, under the assumption that other TSV software can't handle this.
        export.layer.terms.forEach {
            // Explicitly non-null.
            out.write("${it.literals}\t${it.lemmaOrEmpty}\t${it.posOrEmpty}\n".encodeToByteArray())
        }
    }
}