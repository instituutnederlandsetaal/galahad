package org.ivdnt.galahad.formats.txt

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.LayerReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import java.io.File

/**
 * Reads a .txt file and creates an [Layer] from it.
 * [SentenceLayer] and [ParagraphLayer] are supported.
 * .txt files don't support more than one document, so only 1 [DocumentLayer].
 */
class TxtReader(
    val file: File
) : LayerReader() {
    override fun read(): Layer {
        file.forEachLine {
            if (it.isNotBlank()) {
                // split on whitespace
                for (word in it.split(Regex("""\s+"""))) {
                    terms += Term(wordID(), offset, mapOf(Annotation.TOKEN to word))
                    offset += word.length + 1 // +1 for space/LF
                }
                /** Create a [SentenceLayer] after each LF. */
                newSentence()
            } else if (sentences.isNotEmpty()) {
                /** Create a [ParagraphLayer] after a blank line. */
                newParagraph()
            }
        }
        /** Create a single [DocumentLayer]. */
        newDocument()
        return Layer(documents.toTypedArray())
    }
}