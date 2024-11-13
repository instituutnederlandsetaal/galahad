package org.ivdnt.galahad.port.conllu.export

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.LayerConverter
import org.ivdnt.galahad.port.LayerTransformer
import java.io.OutputStream

/**
 * Export a layer to a CoNLLU file. Uses _ for null values. Splits PoS in head and features.
 */
class LayerToConlluConverter(
    transformMetadata: DocumentTransformMetadata,
) : LayerConverter, LayerTransformer(transformMetadata) {

    override val format: DocumentFormat
        get() = DocumentFormat.Conllu

    override fun convert(outputStream: OutputStream) {
        val annotationTypes: List<AnnotationType> = transformMetadata.tagger.expensiveGet().annotationTypes

        val indexer: (Int, Term) -> String
        if (annotationTypes.contains(AnnotationType.ID)) {
            indexer = { _, term -> term.annotations[AnnotationType.ID] ?: "_" }
        } else {
            indexer = { index, _ -> (index + 1).toString() }
        }

        result.terms.forEachIndexed { index, term ->
            val i = indexer(index, term)
            if (i == "1") outputStream.write("\n".encodeToByteArray())

            val upos = term.annotationHead(AnnotationType.UPOS) ?: "_"
            val feats = Term.features(term.annotations[AnnotationType.UPOS]) ?: "_"

            val row = listOf(
                i, // index
                term.annotations[AnnotationType.TOKEN], // form
                term.annotations[AnnotationType.LEMMA] ?: "_",
                upos, // upos
                term.annotations[AnnotationType.POS] ?: "_", // xpos
                feats, // feats
                term.annotations[AnnotationType.HEAD] ?: "_", // head
                term.annotations[AnnotationType.DEPREL] ?: "_", // deprel
                "_", // deps
                term.annotations[AnnotationType.MISC] ?: "_", // misc
            )
            outputStream.write("${row.joinToString("\t")}\n".encodeToByteArray())
        }
    }
}