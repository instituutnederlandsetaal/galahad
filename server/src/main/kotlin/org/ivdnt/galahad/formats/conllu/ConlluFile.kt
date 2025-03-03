package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.conllu.export.ConlluLayerMerger
import org.ivdnt.galahad.formats.tsv.TSVFile
import java.io.File

/**
 * CoNLL-U is basically TSV, so use it as a basis.
 */
class ConlluFile(file: File) : TSVFile(file) {
    override val format = DocumentFormat.Conllu

    val featsIndex = 5
    val uposIndex = 3
    val miscIndex = 9
    val nerIndex = 10
    override val columnIndices: MutableMap<AnnotationType, Int> = mutableMapOf(
        AnnotationType.ID to 0,
        AnnotationType.TOKEN to 1,
        AnnotationType.LEMMA to 2,
        AnnotationType.UPOS to uposIndex,
        AnnotationType.POS to 4, // XPOS
        AnnotationType.HEAD to 6,
        AnnotationType.DEPREL to 7,
        AnnotationType.MISC to 9,
        AnnotationType.NER to 10,
    )
    /** Supported names for the ner attribute in the MISC column. */
    private val nerAttrNames: List<String> = listOf("NamedEntity", "ner")

    // CoNLL-U has a fixed order of columns.
    override fun getColumnIndices(headers: List<String>) {}

    /**
     * For CoNLL-U we need to manually combine the head pos with its features.
     */
    fun getPos(values: List<String>): String? {
        val head: String = getGenericColumn(uposIndex, values) ?: return null // if no head, ignore features and return
        val features: String? = getGenericColumn(featsIndex, values)
        return if (features != null) {
            "$head($features)"
        } else {
            head
        }
    }

    /**
     * Retrieve the NER from the MISC column and convert it to IOB.
     */
    fun getNER(values: List<String>): String? {
        val misc = getGenericColumn(miscIndex, values) ?: return null

        // nerKeyValue is for example "NamedEntity=S-LOC"
        val nerKeyValue: String = misc.split("|").firstOrNull { nerAttrNames.contains(it.split("=").first()) } ?: return null
        val nerValue: String = nerKeyValue.substringAfter('=')

        // convert to IOB
        // Replace /^S\-/ with B- and /^E\-/ with I-.
        // E.g.: S-LOC -> B-LOC, E-LOC -> I-LOC
        val replaceS: Regex = Regex("^S-")
        val replaceE: Regex = Regex("^E-")
        val nerIOB = nerValue.replace(replaceS, "B-").replace(replaceE, "I-")

        return nerIOB
    }

    override fun getColumn(index: Int?, values: List<String>): String? {
        if (index == uposIndex) return getPos(values)
        if (index == nerIndex) return getNER(values)
        return getGenericColumn(index, values)
    }

    /**
     * Get conllu columns and treat "_" as null.
     */
    private fun getGenericColumn(index: Int?, values: List<String>): String? {
        return super.getColumn(index, values).takeIf { it != "_" }
    }

    override fun merge(transformMetadata: DocumentTransformMetadata): ConlluFile {
        // Sets header indices needed to merge.
        parse()
        return ConlluLayerMerger(this, transformMetadata).merge()
    }
}