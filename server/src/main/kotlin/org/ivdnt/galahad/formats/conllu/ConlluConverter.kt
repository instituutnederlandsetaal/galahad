package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerConverter
import java.io.OutputStream
import java.io.PrintWriter

class ConlluConverter(export: DocumentExport) : LayerConverter(export) {
    override fun convert(out: OutputStream): Unit = convert(PrintWriter(out.bufferedWriter()))

    private fun convert(out: PrintWriter) {
        // write the annotation layer to the output stream
        export.layer.documents.forEach { doc ->
            out.println("# newdoc id = ${doc.id}")
            doc.paragraphs.forEach { par ->
                out.println("# newpar id = ${par.id}")
                par.sentences.forEach { sent ->
                    out.println("# sent_id = ${sent.id}")
                    out.println("# text = $sent")
                    sent.terms.forEachIndexed { i, t ->
                        val token = t.token
                        val lemma = t.lemma ?: "_"
                        val upos = t.upos ?: "_"
                        val xpos = t.pos?.let { Term.singlePosToHead(it) } ?: "_"
                        val feats = t.pos?.let { Term.features(it) } ?: "_"
                        val deprel = t.deprel ?: "_"
                        val head = t.head ?: "_"

                        // Misc in the form spaceAfter=NO|NamedEntity=ORG (depending on the nullable values)
                        val spaceAfter = if (!t.spaceAfter) "SpaceAfter=NO" else null
                        val ner = t.ner?.let { "NamedEntity=$it" }
                        val misc = listOfNotNull(spaceAfter, ner).joinToString("|").ifEmpty { "_" }

                        val fields = listOf(i + 1, token, lemma, upos, xpos, feats, head, deprel, misc)
                        out.println(fields.joinToString("\t"))
                    }
                    out.println() // empty line between sentences
                }
            }
        }
    }
}