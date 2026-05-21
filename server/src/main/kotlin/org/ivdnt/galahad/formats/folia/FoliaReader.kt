package org.ivdnt.galahad.formats.folia

import java.io.InputStream
import org.ivdnt.galahad.formats.reader.XmlReader

class FoliaReader(stream: InputStream) : XmlReader(stream) {
    override val documentTags: Array<String> = DOCUMENT_TAGS
    override val paragraphTags: Array<String> = PARAGRAPH_TAGS
    override val sentenceTags: Array<String> = SENTENCE_TAGS
    override val wordTags: Array<String> = WORD_TAGS
    override val ignorableTags: Array<String> = IGNORABLE_TAGS
    override val nerTags: Array<String> = NER_TAGS
    override val depTags: Array<String> = DEP_TAGS

    override fun parseAttrs() {
        when (reader.localName) {
            "pos" -> pos = reader.getAttributeValue(null, "class")
            "lemma" -> lemma = reader.getAttributeValue(null, "class")
            "w" -> spaceAfter = reader.getAttributeValue(null, "space") != "no"
            "entity" -> nerValue = reader.getAttributeValue(null, "class")
            "dependency" -> deprel = reader.getAttributeValue(null, "class")
            "wref" -> {
                reader.getAttributeValue(null, "id")?.let {
                    when {
                        "entity" in xmlPath -> nerTargets += terms.indexOfFirst { t -> t.id == it }
                        "dep" in xmlPath -> deprelTo = it
                        "hd" in xmlPath -> deprelFrom = it
                    }
                }
            }
        }
    }

    companion object {
        // top most <text> or <speech> defines a document, any other is treated as a paragraph
        private val DOCUMENT_TAGS = arrayOf("text", "speech")
        private val PARAGRAPH_TAGS =
            arrayOf("text", "speech", "div", "p", "head", "list", "item", "event", "table", "part")
        private val SENTENCE_TAGS = arrayOf("s", "utt")
        private val WORD_TAGS = arrayOf("w")
        private val IGNORABLE_TAGS =
            arrayOf("morphology", "note", "figure", "comment", "original", "suggestion", "metadata")
        private val NER_TAGS = arrayOf("entity")
        private val DEP_TAGS = arrayOf("dependency")
    }
}
