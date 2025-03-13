package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.AnnotationLayer
import org.ivdnt.galahad.annotations.AnnotationType
import org.ivdnt.galahad.annotations.Term2
import org.ivdnt.galahad.annotations.WordForm
import org.ivdnt.galahad.formats.AnnotationReader
import java.io.File

class ConlluReader(
    file: File
) : AnnotationReader(file) {

    private val ignorableMultiWordIds: MutableSet<String> = mutableSetOf()
    val indices: Map<AnnotationType, Int> = mapOf(
        AnnotationType.LEMMA to 2,
        AnnotationType.POS to 4,
        AnnotationType.HEAD to 6,
        AnnotationType.DEPREL to 7,
        AnnotationType.UPOS to -1, // see GetUpos
        AnnotationType.NER to -1, // see GetNer
    )

    override fun read(): AnnotationLayer {
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
        return AnnotationLayer(documents)
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

        newWordform(fields)
        newTerm(fields)
    }

    private fun newTerm(fields: List<String>) {
        val wordform = wordforms.last()
        for (column in indices.keys) {
            getColumn(column, fields)?.let { terms.add(column, Term2(it, listOf(wordform))) }
        }
    }

    private fun getColumn(annotationType: AnnotationType, fields: List<String>): String? {
        if (annotationType == AnnotationType.UPOS) return getUpos(fields)
//        if (annotationType == AnnotationType.NER) return getNER(fields)
        return getColumn(indices[annotationType]!!, fields)
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

    private fun newWordform(fields: List<String>) {
        val spaceAfter = !fields[9].contains("SpaceAfter=No")
        val wordForm = WordForm(
            id = fields[0], // id
            literal = fields[1], // form
            offset = offset, spaceAfter = spaceAfter
        )
        offset += fields[1].length
        if (spaceAfter) offset++ // add space after
        wordforms.add(wordForm)
    }
}