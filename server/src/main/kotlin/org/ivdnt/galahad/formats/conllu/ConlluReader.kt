package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.*
import java.io.File

class ConlluReader(
    private val file: File
) {
    val layer: AnnotationLayer by lazy { read() }

    private fun read(): AnnotationLayer {
        // conllu defines the following document structure:
        // # newdoc id = <doc_id>
        // # newpar id = <par_id>
        // # sent_id = <sent_id>
        // but all of these are optional (yes, even sent_id)
        // and even if the newdoc/newpar is preset, the id is optional

        val documents = mutableListOf<DocumentLayer>()
        var docIDStr = "d1"
        val paragraphs = mutableListOf<ParagraphLayer>()
        var parIDStr = "p1"
        val sentences = mutableListOf<SentenceLayer>()
        var sentIDStr = "s1"
        val wordforms = mutableListOf<WordForm>()
        var offset = 0
        file.forEachLine {
            if (it.startsWith("# newdoc")) {

                // create new document
                if (sentences.isNotEmpty()) {
                    paragraphs.add(ParagraphLayer(parIDStr, sentences.toList()))
                    sentences.clear()
                }
                if (paragraphs.isNotEmpty()) {
                    documents.add(DocumentLayer(docIDStr, paragraphs.toList()))
                    paragraphs.clear()
                }
                // get the docID last, so we don't overwrite the previous docID
                val docID: String? = Regex("id = (\\S+)").find(it)?.groupValues?.get(1)
                docIDStr = docID ?: "d${documents.size + 1}"

            } else if (it.startsWith("# newpar")) {


                // create new paragraph
                if (sentences.isNotEmpty()) {
                    paragraphs.add(ParagraphLayer(parIDStr, sentences.toList()))
                    sentences.clear()
                    val parID: String? = Regex("id = (\\S+)").find(it)?.groupValues?.get(1)
                    parIDStr = parID ?: "p${paragraphs.size + 1}"
                }

                val parID: String? = Regex("id = (\\S+)").find(it)?.groupValues?.get(1)
                parIDStr = parID ?: "p${paragraphs.size + 1}"

            } else if (it.startsWith("# sent_id")) {


                // create new sentence
                if (wordforms.isNotEmpty()) {
                    sentences.add(SentenceLayer(sentIDStr, wordforms.toList()))
                    wordforms.clear()
                }

                offset = 0
                val sentID: String? = Regex("id = (\\S+)").find(it)?.groupValues?.get(1)
                sentIDStr = sentID ?: "s${sentences.size + 1}"


            } else if (it.isBlank()) {
                // create sentence for the last wordforms
                if (wordforms.isNotEmpty()) {
                    sentences.add(SentenceLayer(sentIDStr, wordforms.toList()))
                    wordforms.clear()
                }

                offset = 0
                val sentID: String? = Regex("id = (\\S+)").find(it)?.groupValues?.get(1)
                sentIDStr = sentID ?: "s${sentences.size + 1}"
            } else if (!it.startsWith("#")) {
                // split on whitespace
                val fields = it.split("\\s+".toRegex())
                val wordForm = WordForm(
                    id = fields[0], // id
                    literal = fields[1], // form
                    offset = offset,
                    length = fields[1].length, // length of form
                )
                offset += fields[1].length + 1 // +1 for the space
                wordforms.add(wordForm)
            }
        }
        // create paragraph for the last sentences
        if (wordforms.isNotEmpty()) {
            sentences.add(SentenceLayer(sentIDStr, wordforms.toList()))
        }
        if (sentences.isNotEmpty()) {
            paragraphs.add(ParagraphLayer(parIDStr, sentences.toList()))
        }
        if (paragraphs.isNotEmpty()) {
            documents.add(DocumentLayer(docIDStr, paragraphs.toList()))
        }
        return AnnotationLayer(documents)
    }
}