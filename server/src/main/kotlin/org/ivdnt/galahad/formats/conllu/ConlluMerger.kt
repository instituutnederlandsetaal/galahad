package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.formats.tsv.TsvMerger

/**
 * Merge a layer with a CoNLLU file. Uses _ for null values. Splits PoS in head and features.
 * Do not call directly. Use [ConlluFile.merge] instead.
 */
internal class ConlluMerger(
    override val sourceFile: ConlluFile, transformMetadata: DocumentExport,
) : TsvMerger(sourceFile, transformMetadata) {

    override val hasHeader: Boolean = false

    override fun merge(): ConlluFile {
        sourceFile.parse() // parse the sourceFile if needed.
        parseByLine()
        return ConlluFile(outFile)
    }

    override fun mergeSingleColumn(
        columns: MutableList<String>,
        layer: Layer,
        termIndex: Int,
        annotation: Annotation,
        columnIndex: Int,
    ) {
        when (annotation) {
            Annotation.MISC -> {
                // construct MISC by combining NER and MISC
                val term: Term = layer.terms[termIndex]
                var ner: String? = term.annotations[Annotation.NER]?.let { "NamedEntity=$it" }
                val misc: String? = term.annotations[Annotation.MISC]
                val miscField: String = listOfNotNull(ner, misc).joinToString("|")
                columns[columnIndex] = miscField.ifEmpty { "_" }
            }

            Annotation.NER -> return // NER is already in MISC
            Annotation.UPOS -> {
                // Split UPOS into head and features
                val term: Term = layer.terms[termIndex]
                val head: String = term.annotationHead(Annotation.UPOS) ?: "_"
                val features: String = Term.features(term.annotations[Annotation.UPOS]) ?: "_"
                columns[sourceFile.uposIndex] = head
                columns[sourceFile.featsIndex] = features
            }

            Annotation.ID -> return // Don't overwrite
            else -> super.mergeSingleColumn(columns, layer, termIndex, annotation, columnIndex)
        }
    }
}