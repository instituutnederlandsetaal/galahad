package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.formats.reader.XmlReader
import java.io.InputStream

class TeiReader(stream: InputStream) : XmlReader(stream) {
    override val nerTags: Array<String> = NER_TAGS
    override val documentTags: Array<String> = DOCUMENT_TAGS
    override val paragraphTags: Array<String> = PARAGRAPH_TAGS
    override val sentenceTags: Array<String> = SENTENCE_TAGS
    override val wordTags: Array<String> = WORD_TAGS
    override val ignorableTags: Array<String> = IGNORABLE_TAGS
    override val depTags: Array<String> = DEP_TAGS
    private val wordDataTags: Array<String> = WORD_DATA_TAGS

    override fun parseAttrs() {
        when (reader.localName) {
            in wordDataTags -> {
                lemma = reader.getAttributeValue(null, "lemma")?.ifBlank { null }
                pos =
                    reader.getAttributeValue(null, "pos")?.ifBlank { null }
                        ?: reader.getAttributeValue(null, "type")?.ifBlank { null }
                spaceAfter = reader.getAttributeValue(null, "join") !in arrayOf("right", "both")
            }

            in GROUP_TAGS -> {
                group = reader.getAttributeValue(null, "n")?.ifBlank { null }
            }

            in NER_TAGS -> {
                nerValue = reader.getAttributeValue(null, "type")
            }

            "link" -> {
                deprel = reader.getAttributeValue(null, "ana")?.replace("ud-syn:", "")
                reader.getAttributeValue(null, "target")?.split(" ")?.also {
                    deprelFrom = it[0].substring(1) // ignore initial #
                    deprelTo = it[1].substring(1)
                }
            }
        }
    }

    companion object {
        private val DOCUMENT_TAGS = arrayOf("text")
        val WORD_TAGS = arrayOf("w", "pc", "seg")
        private val GROUP_TAGS = arrayOf("join")
        private val WORD_DATA_TAGS = arrayOf("w", "pc")
        private val SENTENCE_TAGS = arrayOf("s", "l", "u")
        val IGNORABLE_TAGS =
            arrayOf(
                "note",
                "listBibl",
                "listWit",
                "figure",
                "xr",
                "fs",
                "teiHeader",
                "incident",
                "interpGrp",
                "pb",
            )
        private val NER_TAGS = arrayOf("name")
        private val DEP_TAGS = arrayOf("link")
        private val PARAGRAPH_TAGS =
            arrayOf(
                "text", // top most <text> defines a document, any other <text> is treated as a
                // paragraph
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
                "floatingText",
                "said",
            )
    }
}
