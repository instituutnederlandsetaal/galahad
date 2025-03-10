package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.annotations.*
import org.ivdnt.galahad.util.getXmlBuilder
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File

class TeiReader(
    private val file: File
) {
    val layer: AnnotationLayer by lazy { read() }
    val doc: Document by lazy { getXmlBuilder().parse(file) }

    private val documents = mutableListOf<DocumentLayer>()
    private val paragraphs = mutableListOf<ParagraphLayer>()
    private val sentences = mutableListOf<SentenceLayer>()
    private val wordforms = mutableListOf<WordForm>()

    fun read(): AnnotationLayer {
        // we will parse documents, paragraphs, sentences, and wordforms
        // example document
        // <TEI> <!-- root, may contain multiple docs -->
        //   <text xml:id="d1"> <!-- document, contains multiple paragraphs -->
        //     <body>
        //       <p xml:id="d1.p1"> <!-- paragraph, contains multiple sentences -->
        //         <s>
        //           <w xml:id="d1.p1.w1">word1</w>
        //           <w>word2</w>
        //         </s>
        //       </p>
        //     </body>
        //   </text>
        // </TEI>

        val root = doc.documentElement
        val docs = root.getElementsByTagName("text")
        for (i in 0 until docs.length) {
            var offset = 0
            val doc = docs.item(i) as Element
            val docId = doc.getAttribute("xml:id")
            val parElems = doc.getElementsByTagName("p")
            for (j in 0 until parElems.length) {
                val paragraph = parElems.item(j) as Element
                val paragraphId = paragraph.getAttribute("xml:id")
                val sentElems = paragraph.getElementsByTagName("s")
                for (k in 0 until sentElems.length) {
                    val sentence = sentElems.item(k) as Element
                    val words = sentence.getElementsByTagName("w")
                    for (l in 0 until words.length) {
                        val word = words.item(l) as Element
                        val wordId = word.getAttribute("xml:id")
                        val text = word.textContent
                        wordforms.add(
                            WordForm(
                                id = wordId, literal = text, length = text.length, offset = offset
                            )
                        )
                        offset += text.length + 1
                    }
                    sentences.add(SentenceLayer(sentence.getAttribute("xml:id"), wordforms.toList()))
                    wordforms.clear()
                }
                paragraphs.add(ParagraphLayer(paragraphId, sentences.toList()))
                sentences.clear()
            }
            documents.add(DocumentLayer(docId, paragraphs.toList()))
            paragraphs.clear()
        }

        return AnnotationLayer(documents)
    }
}