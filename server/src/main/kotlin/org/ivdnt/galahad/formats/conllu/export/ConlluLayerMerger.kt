package org.ivdnt.galahad.formats.conllu.export

import org.ivdnt.galahad.annotations.AnnotationType
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.conllu.ConlluFile
import org.ivdnt.galahad.formats.tsv.export.TSVLayerMerger

/**
 * Merge a layer with a CoNLLU file. Uses _ for null values. Splits PoS in head and features.
 * Do not call directly. Use [ConlluFile.merge] instead.
 */
internal class ConlluLayerMerger(
    override val sourceFile: ConlluFile, transformMetadata: DocumentTransformMetadata,
) : TSVLayerMerger(sourceFile, transformMetadata) {

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
        annotationType: AnnotationType,
        columnIndex: Int,
    ) {
        when (annotationType) {
            AnnotationType.MISC -> {
                // construct MISC by combining NER and MISC
                val term: Term = layer.terms[termIndex]
                var ner: String? = term.annotations[AnnotationType.NER]?.let { "NamedEntity=$it" }
                val misc: String? = term.annotations[AnnotationType.MISC]
                val miscField: String = listOfNotNull(ner, misc).joinToString("|")
                columns[columnIndex] = miscField.ifEmpty { "_" }
            }

            AnnotationType.NER -> return // NER is already in MISC
            AnnotationType.UPOS -> {
                // Split UPOS into head and features
                val term: Term = layer.terms[termIndex]
                val head: String = term.annotationHead(AnnotationType.UPOS) ?: "_"
                val features: String = Term.features(term.annotations[AnnotationType.UPOS]) ?: "_"
                columns[sourceFile.uposIndex] = head
                columns[sourceFile.featsIndex] = features
            }

            AnnotationType.ID -> return // Don't overwrite
            else -> super.mergeSingleColumn(columns, layer, termIndex, annotationType, columnIndex)
        }
    }
}