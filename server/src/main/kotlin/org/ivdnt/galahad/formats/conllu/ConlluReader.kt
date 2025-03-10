package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.*
import java.io.File

class ConlluReader(
    private val file: File
) {
    val layer: AnnotationLayer by lazy { read() }

    private val documents = mutableListOf<DocumentLayer>()
    private val paragraphs = mutableListOf<ParagraphLayer>()
    private val sentences = mutableListOf<SentenceLayer>()
    private val wordforms = mutableListOf<WordForm>()

    private val ignorableMultiWordIds: MutableSet<String> = mutableSetOf()

    private var docIDStr = "d1"
    private var parIDStr = "p1"
    private var sentIDStr = "s1"
    private var offset = 0

    private fun newDocument() {
        newParagraph()
        if (paragraphs.isNotEmpty()) {
            documents.add(DocumentLayer(docIDStr, paragraphs.toList()))
            paragraphs.clear()
        }
    }

    private fun newParagraph() {
        newSentence()
        if (sentences.isNotEmpty()) {
            paragraphs.add(ParagraphLayer(parIDStr, sentences.toList()))
            sentences.clear()
        }
    }

    private fun newSentence() {
        if (wordforms.isNotEmpty()) {
            sentences.add(SentenceLayer(sentIDStr, wordforms.toList()))
            wordforms.clear()
        }
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

    private fun read(): AnnotationLayer {
        file.forEachLine {
            when {
                it.startsWith("# newdoc") -> {
                    newDocument()
                    // get ID last, so we don't overwrite it while creating a new unit
                    docIDStr = Regex("id = (\\S+)").find(it)?.groupValues?.get(1) ?: "d${documents.size + 1}"
                }

                it.startsWith("# newpar") -> {
                    newParagraph()
                    // get ID last, so we don't overwrite it while creating a new unit
                    parIDStr = Regex("id = (\\S+)").find(it)?.groupValues?.get(1) ?: "p${paragraphs.size + 1}"
                }

                it.startsWith("# sent_id") -> {
                    newSentence()
                    sentIDStr = Regex("id = (\\S+)").find(it)?.groupValues?.get(1) ?: "s${sentences.size + 1}"
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
}