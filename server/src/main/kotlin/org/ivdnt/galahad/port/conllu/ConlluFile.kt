package org.ivdnt.galahad.port.conllu

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.conllu.export.ConlluLayerMerger
import org.ivdnt.galahad.port.tsv.TSVFile
import java.io.File

/**
 * CoNLL-U is basically TSV, so use it as a basis.
 */
class ConlluFile(file: File) : TSVFile(file) {
    override val format = DocumentFormat.Conllu

    private val featsIndex = 5
    private val uposIndex = 3
    override val columnIndices: MutableMap<AnnotationType, Int> = mutableMapOf(
        AnnotationType.ID to 0,
        AnnotationType.TOKEN to 1,
        AnnotationType.LEMMA to 2,
        AnnotationType.UPOS to uposIndex,
        AnnotationType.POS to 4, // XPOS
        AnnotationType.HEAD to 6,
        AnnotationType.DEPREL to 7,
        AnnotationType.MISC to 9
    )

    // CoNLL-U has a fixed order of columns.
    override fun getColumnIndices(headers: List<String>, errors: MutableList<String>) {}

    /**
     * For CoNLL-U we need to manually combine the head pos with its features.
     */
    fun getPos(values: List<String>): String {
        val head: String = super.getColumn(uposIndex, values) ?: return "_"
        val features: String? = super.getColumn(featsIndex, values)
        return if (!features.isNullOrBlank() && features != "_") {
            "$head($features)"
        } else {
            head
        }
    }

    override fun getColumn(index: Int?, values: List<String>): String? {
        if (index == uposIndex) return getPos(values)
        val value = super.getColumn(index, values)
        return if (value == "_") null else value
    }

    override fun merge(transformMetadata: DocumentTransformMetadata): ConlluFile {
        // Sets header indices needed to merge.
        parse()
        return ConlluLayerMerger(this, transformMetadata).merge()
    }
}