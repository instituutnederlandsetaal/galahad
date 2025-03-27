package org.ivdnt.galahad.formats.naf

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.util.XmlUtil
import org.ivdnt.galahad.util.childElements
import org.ivdnt.galahad.util.childOrNull
import java.io.File

typealias WordformID = String
typealias TermID = String

class NafReader(file: File) : AnnotationReader(file) {
    private val xml = XmlUtil.builder.parse(file)
    private val root = xml.documentElement
    private val nafWordforms = root.childOrNull("text")!!.childElements.map {
        NafWordform(
            id = it.getAttribute("id"),
            offset = it.getAttribute("offset").toInt(),
            token = it.textContent,
            sent = it.getAttribute("sent").toInt(),
            para = it.getAttribute("para").toIntOrNull()
        )
    }
    private val nafTerms = root.childOrNull("terms")!!.childElements.map {
        NafTerm(
            id = it.getAttribute("id"),
            lemma = it.getAttribute("lemma").ifEmpty { null },
            pos = it.getAttribute("pos").ifEmpty { null },
            targets = it.childElements.first().childElements.map { it.getAttribute("id") }.toList()
        )
    }
    private val nafDeps = root.childOrNull("deps")?.childElements?.map {
        NafDep(
            from = it.getAttribute("from"), to = it.getAttribute("to"), rfunc = it.getAttribute("rfunc")
        )
    }
    private val nafEntities = root.childOrNull("entities")?.childElements?.map {
        NafEntity(
            type = it.getAttribute("type").ifEmpty { null },
            references = it.childElements.map { it.childElements.map { it.getAttribute("id") }.toList() }.toList()
        )
    }
    private val id = root.childOrNull("nafHeader")?.childOrNull("public")?.getAttribute("publicId").orEmpty().ifEmpty { null }

    override fun read(): Layer {
        // group wordforms paragraph, then sentence, then sort by offset in sentence
        val grouped = nafWordforms.groupBy { it.para }
            .map { it.value.groupBy { it.sent }.map { it.value.sortedBy { it.offset } } }
        grouped.forEach { para ->
            para.forEach { sent ->
                sent.forEachIndexed { i, wordform ->
                    // retrieve term, entity, and dependencies
                    val term = nafTerms.find { it.targets.contains(wordform.id) }!!
                    val entity = nafEntities?.find { it.references.any { it.contains(term.id) } }
                    val dep = nafDeps?.first { it.to == term.id }

                    // annotations
                    val annotations = mutableMapOf<Annotation, String>()
                    annotations[Annotation.TOKEN] = wordform.token
                    term.lemma?.let { annotations[Annotation.LEMMA] = it }
                    term.pos?.let { annotations[Annotation.POS] = it }
                    entity?.type?.let { annotations[Annotation.NER] = it }
                    dep?.rfunc?.let { annotations[Annotation.DEPREL] = it }
                    val headTerm = nafTerms.find { it.id == dep?.from }
                    val headWordform = nafWordforms.find { it.id == headTerm?.targets?.first() }
                    headWordform?.id?.let { annotations[Annotation.HEAD] = it }

                    // space after
                    val nextWordform = sent.getOrNull(i + 1)
                    val spaceAfter = nextWordform?.offset == wordform.offset + wordform.token.length

                    terms += Term(wordform.id, wordform.offset, annotations, spaceAfter)
                }
                // TODO sentence level spans
                newSentence()
            }
            newParagraph()
        }
        newDocument()
        return Layer(documents)
    }

    data class NafWordform(
        val id: WordformID, val offset: Int, val token: String, val sent: Int, val para: Int?
    )

    data class NafTerm(
        val id: TermID, val lemma: String?, val pos: String?, val targets: List<WordformID>
    )

    data class NafDep(
        val from: TermID,
        val to: TermID,
        val rfunc: String,
    )

    data class NafEntity(
        val type: String?,
        val references: List<List<TermID>>,
    )
}

