package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.annotations.*
import java.io.File

/**
 * Reads a .txt file and creates an [AnnotationLayer] from it.
 * [SentenceLayer] and [ParagraphLayer] are supported. .txt files don't support more than one document, so no [DocumentLayer].
 */
class TxtReader(
    private val file: File
) {
    val layer: AnnotationLayer by lazy { read() }

    private fun read(): AnnotationLayer {
        // Create a new [SentenceLayer] after each newline,
        // and a new [ParagraphLayer] after more than one newline.
        val sentences = mutableListOf<SentenceLayer>()
        val paragraphs = mutableListOf<ParagraphLayer>()
        var wordID = 1
        var sentenceID = 1
        var paragraphID = 1
        file.forEachLine {
            if (it.isNotBlank()) {
                // split on whitespace
                val wordsInLine = it.split("\\s+".toRegex())
                var offset = 0
                val words = mutableListOf<WordForm>()
                for (word in wordsInLine) {
                    words.add(WordForm(word, offset,"w$wordID"))
                    offset += word.length + 1 // +1 for the space
                    wordID++
                }
                // create sentence
                sentences.add(SentenceLayer("s$sentenceID", words, emptyMap()))
                sentenceID++
            } else if (sentences.isNotEmpty()) {
                // create paragraph
                paragraphs.add(ParagraphLayer("p$paragraphID", sentences.toList()))
                sentences.clear() // NOTE: we just copied the list (toList()), so we can clear it
                sentenceID = 1
                paragraphID++
            }
        }
        // create paragraph for the last sentences
        if (sentences.isNotEmpty()) {
            paragraphs.add(ParagraphLayer("p$paragraphID", sentences))
        }
        val doc = DocumentLayer("d1", paragraphs)
        return AnnotationLayer(listOf(doc))
    }
}