package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.formats.xml.XmlReader
import java.io.InputStream

class AaltoFoliaReader(
    stream: InputStream,
) : XmlReader(stream) {
    override val documentTags: Array<String> = DOCUMENT_TAGS
    override val paragraphTags: Array<String> = PARAGRAPH_TAGS
    override val sentenceTags: Array<String> = SENTENCE_TAGS
    override val wordTags: Array<String> = WORD_TAGS
    override val ignorableTags: Array<String> = IGNORABLE_TAGS
    override val wordDataTags: Array<String> = WORD_DATA_TAGS

    override fun parseWordData() {
        when (reader.localName) {
            "pos" -> pos = reader.getAttributeValue(null, "class")
            "lemma" -> lemma = reader.getAttributeValue(null, "class")
            "w" -> spaceAfter = reader.getAttributeValue(null, "space") != "no"
        }
    }

    companion object {
        // top most <text> or <speech> defines a document, any other is treated as a paragraph
        private val DOCUMENT_TAGS = arrayOf("text", "speech")
        private val PARAGRAPH_TAGS =
            arrayOf("text", "speech", "div", "p", "head", "list", "item", "event", "table", "part")
        private val SENTENCE_TAGS = arrayOf("s", "utt")
        private val WORD_TAGS = arrayOf("w")
        private val WORD_DATA_TAGS = arrayOf("w", "lemma", "pos")
        private val IGNORABLE_TAGS = arrayOf("morphology", "note", "figure", "comment", "original", "suggestion")
    }
}
