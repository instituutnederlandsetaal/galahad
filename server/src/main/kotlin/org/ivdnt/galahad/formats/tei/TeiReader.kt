package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.formats.xml.XmlReader
import java.io.BufferedInputStream

class TeiReader(
    stream: BufferedInputStream,
) : XmlReader(stream) {
    override val documentTags: Array<String> = DOCUMENT_TAGS
    override val paragraphTags: Array<String> = PARAGRAPH_TAGS
    override val sentenceTags: Array<String> = SENTENCE_TAGS
    override val wordTags: Array<String> = WORD_TAGS
    override val ignorableTags: Array<String> = IGNORABLE_TAGS
    override val wordDataTags: Array<String> = WORD_DATA_TAGS

    override fun parseAttrs() {
        lemma = reader.getAttributeValue(null, "lemma")?.takeIf { it.isNotBlank() }
        pos =
            reader.getAttributeValue(null, "pos")?.takeIf { it.isNotBlank() } ?: reader.getAttributeValue(null, "type")
                ?.takeIf { it.isNotBlank() }
        spaceAfter = reader.getAttributeValue(null, "join") !in arrayOf("right", "both")
    }

    companion object {
        private val DOCUMENT_TAGS = arrayOf("text")
        private val WORD_TAGS = arrayOf("w", "pc")
        private val WORD_DATA_TAGS = arrayOf("w", "pc")
        private val SENTENCE_TAGS = arrayOf("s", "l", "u")
        private val IGNORABLE_TAGS = arrayOf("note", "listBibl", "listWit", "figure", "xr", "fs", "del")
        private val PARAGRAPH_TAGS = arrayOf(
            "text", // top most <text> defines a document, any other <text> is treated as a paragraph
            "body",
            "front",
            "back",
            "head",
            "opener",
            "signed",
            "closer",
            "postscript",
            "signed",
            "trailer",
            "argument",
            "byline",
            "dateline",
            "docAuthor",
            "docDate",
            "epigraph",
            "meeting",
            "salute",
            "div",
            "div1",
            "div2",
            "div3",
            "div4",
            "div5",
            "div6",
            "div7",
            "lg",
            "ab",
            "p",
            "cit",
            "quote",
            "floatingText",
            "said"
        )
    }
}
