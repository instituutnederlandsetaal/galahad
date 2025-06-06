package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import java.io.OutputStream
import java.io.PrintWriter

class TsvConverter(export: DocumentExport) : LayerConverter(export) {
    override fun convert(out: OutputStream): Unit = convert(PrintWriter(out))

    private fun convert(out: PrintWriter) {
        val header: List<Annotation> = Annotation.order(export.tagger.annotations)
        out.println("id\t" + header.joinToString("\t"))
        // We only write sentence boundaries (\n) and no #-comments, under the assumption that other TSV software can't handle this.
        documents.forEachIndexed { docI, doc ->
            doc.paragraphs.forEachIndexed { parI, par ->
                par.sentences.forEachIndexed { sentI, sent ->
                    sent.terms.forEach { term ->
                        val fields = listOf(term.id) + header.map { term.annotations[it] ?: "" }
                        out.println(fields.joinToString("\t"))
                    }
                    // empty line between sentences
                    val isLastSent =
                        docI == documents.lastIndex && parI == doc.paragraphs.lastIndex && sentI == par.sentences.lastIndex
                    if (!isLastSent) out.println()
                }
                // empty line between paragraphs
                val isLastPar = docI == documents.lastIndex && parI == doc.paragraphs.lastIndex
                if (!isLastPar) out.println()
            }
        }
        out.flush()
    }
}