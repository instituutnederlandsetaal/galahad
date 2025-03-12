package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.*
import org.ivdnt.galahad.formats.AnnotationReader
import java.io.File

class ConlluReader(
    file: File
) : AnnotationReader(file) {

    private val ignorableMultiWordIds: MutableSet<String> = mutableSetOf()

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

                it.startsWith("# sent_id") -> {
                    newSentence()
                    sentID = Regex("id = (\\S+)").find(it)?.groupValues?.get(1) ?: "s${sentences.size + 1}"
                }

                it.isBlank() -> {
                    newSentence()
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

        val spaceAfter = !fields[9].contains("SpaceAfter=No")
        val wordForm = WordForm(
            id = fields[0], // id
            literal = fields[1], // form
            offset = offset, length = fields[1].length, // length of form
            spaceAfter = spaceAfter
        )
        offset += fields[1].length
        if (spaceAfter) offset++ // add space after
        wordforms.add(wordForm)
    }
}