package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import java.io.OutputStream
import java.io.PrintWriter

class TsvConverter(export: DocumentExport) : LayerConverter(export) {
    override fun convert(out: OutputStream): Unit = convert(PrintWriter(out.bufferedWriter()))

    private fun convert(out: PrintWriter) {
        val header: List<Annotation> = export.tagger.annotations.toList()
        // force the header list to be in the order of [Annotation.entries]
        header.sortedBy { 1 + Annotation.entries.indexOf(it) } // +1 otherwise index 0 will be last

        out.println(header.joinToString("\t"))
        // We only write sentence boundaries (\n) and no #-comments, under the assumption that other TSV software can't handle this.
        export.layer.documents.forEach { document ->
            document.paragraphs.forEach { paragraph ->
                paragraph.sentences.forEach { sentence ->
                    sentence.terms.forEach { term ->
                        val fields = header.map { term.annotations[it] ?: "" }
                        out.println(fields.joinToString("\t"))
                    }
                    out.println() // empty line between sentences
                }
            }
        }
    }
}