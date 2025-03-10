package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.AnnotationLayer
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Writer

class ConlluConverter(
    private val layer: AnnotationLayer
) {
    fun convert(out: PrintWriter) {
        // write the annotation layer to the output stream
        layer.documents.forEach { doc ->
            out.println("# newdoc id = ${doc.id}")
            doc.paragraphs.forEach { par ->
                out.println("# newpar id = ${par.id}")
                par.sentences.forEach { sent ->
                    out.println("# sent_id = ${sent.id}")
                    out.println("# text = $sent")
                    sent.wordforms.forEach {
                        out.println("${it.id}\t${it.literal}\t_\t_\t_\t_\t_\t_\t_\t_")
                    }
                    out.println()
                }
            }
        }
    }
}