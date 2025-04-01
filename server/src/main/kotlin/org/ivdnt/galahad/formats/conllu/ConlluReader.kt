package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import java.io.File

class ConlluReader(
    file: File
) : AnnotationReader(file) {
    private val ignorableMultiWordIds: MutableSet<String> = mutableSetOf()

    override fun read(): Layer {
        file.forEachLine {
            when {
                it.startsWith("# newdoc") -> {
                    newDocument()
                    // get ID last, so we don't overwrite it while creating a new unit
                    docID = Regex("id = (\\S+)").find(it)?.groupValues?.get(1) ?: "d${documents.size + 1}"
                }

                it.startsWith("# newpar") -> {
                    newParagraph()
                    // get ID last, so we don't overwrite it while creating a new unit
                    parID = Regex("id = (\\S+)").find(it)?.groupValues?.get(1) ?: "p${paragraphs.size + 1}"
                }

                it.startsWith("# sent_id") || it.isBlank() -> {
                    newSentence()
                    sentID = Regex("id = (\\S+)").find(it)?.groupValues?.get(1) ?: "s${sentences.size + 1}"
                }

                !it.startsWith("#") -> {
                    newWord(it)
                }
            }
        }
        // create a document for the remaining tokens
        newDocument()
        return Layer(documents.toTypedArray())
    }

    override fun newSentence() {
        ignorableMultiWordIds.clear()
        super.newSentence()
    }

    private fun parseMultiWordToken(string: String) {
        //val parent = wordforms.last()
        // we could create multiple analysis tokens like PD+NOU-C here.
    }

    private fun newWord(string: String) {
        // split on whitespace
        val fields = string.split("\\s+".toRegex())
        val id = fields[0]
        if (id.contains(".")) return // ignore empty nodes
        if (id.contains("-")) {
            // remember the range of multi-word tokens
            val range = id.split("-")
            val start = range[0].toInt()
            val end = range[1].toInt()
            for (i in start..end) {
                ignorableMultiWordIds.add(i.toString())
            }
        }
        if (id in ignorableMultiWordIds) return parseMultiWordToken(string) // ignore multi-word tokens

        newTerm(fields)
    }

    private fun getColumn(annotation: Annotation, fields: List<String>): String? {
        if (annotation == Annotation.UPOS) return getUpos(fields)
        if (annotation == Annotation.NER) return getNER(fields)
        return getColumn(indices[annotation]!!, fields)
    }

    /**
     * Retrieve the NER from the MISC column and convert it to IOB.
     */
    private fun getNER(values: List<String>): String? {
        val misc = getColumn(9, values) ?: return null

        // nerKeyValue is for example "NamedEntity=S-LOC"
        val nerKeyValue: String =
            misc.split("|").firstOrNull { nerAttrNames.contains(it.split("=").first()) } ?: return null
        val nerValue: String = nerKeyValue.substringAfter('=')

        // convert to IOB
        // Replace /^S\-/ with B- and /^E\-/ with I-.
        // E.g.: S-LOC -> B-LOC, E-LOC -> I-LOC
        val replaceS = Regex("^S-")
        val replaceE = Regex("^E-")
        val nerIOB = nerValue.replace(replaceS, "B-").replace(replaceE, "I-")

        return nerIOB
    }

    // returns null on _
    private fun getColumn(i: Int, fields: List<String>): String? = fields.getOrNull(i)?.takeIf { it != "_" }

    private fun getUpos(fields: List<String>): String? {
        val head: String = getColumn(3, fields) ?: return null // if no head, ignore features and return
        val features: String? = getColumn(5, fields)
        return if (features != null) {
            "$head($features)"
        } else {
            head
        }
    }

    private fun newTerm(fields: List<String>) {
        val spaceAfter = !fields[9].contains("SpaceAfter=No")

        val annotations = mutableMapOf<Annotation, String>()
        for (column in indices.keys) {
            getColumn(column, fields)?.let { annotations[column] = it }
        }
        terms += Term(wordID(), offset, annotations, spaceAfter)
        offset += fields[1].length
        if (spaceAfter) offset++ // add space after
    }

    companion object {
        /** Supported names for the ner attribute in the MISC column. */
        private val nerAttrNames: List<String> = listOf("NamedEntity", "ner")

        private val indices: Map<Annotation, Int> = mapOf(
            Annotation.TOKEN to 1,
            Annotation.LEMMA to 2,
            Annotation.POS to 4,
            Annotation.HEAD to 6,
            Annotation.DEPREL to 7,
            Annotation.UPOS to -1, // see GetUpos
            Annotation.NER to -1, // see GetNer
        )
    }
}