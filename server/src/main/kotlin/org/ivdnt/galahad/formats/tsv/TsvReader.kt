package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import java.io.File

class TsvReader(
    file: File
) : AnnotationReader(file) {
    private val columnIndices: MutableMap<Annotation, Int> = mutableMapOf()

    override fun read(): Layer {
        file.forEachLine { line ->
            if (columnIndices.isEmpty()) {
                parseHeader(line)
            } else {
                parseBody(line)
            }
        }
        newDocument()
        return Layer(documents)
    }

    /**
     * Retrieve the indices of the literal, lemma and PoS columns from the header.
     * If any were not found, throw.
     * @param line Header line read from the tsv file.
     */
    private fun parseHeader(line: String) {
        val headers = line.split("\t")

        getColumnIndices(headers)

        // Check for the presence of a token
        if (columnIndices[Annotation.TOKEN] == null) {
            throw IllegalArgumentException("No token column found in TSV file.")
        }
    }

    /**
     * Set up columnIndices to reflect the header.
     * For each header column, check if it is the name of any of the AnnotationTypes.
     */
    private fun getColumnIndices(
        headers: List<String>,
    ) {
        headers.forEachIndexed { index, header ->
            columnNames.entries
                // from the columnNames, find the first AnnotationType that has a name that matches the header.
                .firstOrNull { (_, names) ->
                    names.any { name -> header.equals(name, ignoreCase = true) }
                    // if it exists, register the index
                }?.let { (annotationType, _) ->
                    columnIndices[annotationType] = index
                }
        }
    }

    private fun parseBody(line: String) {
        if (line.isBlank()) {
            newSentence()
            return
        }

        // Split on tabs
        val values: List<String> = line.split("\t")

        // Retrieve values
        val mutAnnot: MutableMap<Annotation, String> = mutableMapOf()
        for (column in columnIndices.entries) {
            getColumn(column.value, values)?.let { mutAnnot[column.key] = it }
        }
        terms += Term(wordID(), offset, mutAnnot)
        offset += mutAnnot[Annotation.TOKEN]?.length ?: 0
    }

    // Retrieves a column with bounds checking.
    private fun getColumn(index: Int, values: List<String>): String? =
        values.getOrNull(index)?.takeIf { it.isNotBlank() }

    companion object {
        val columnNames: Map<Annotation, List<String>> = mapOf(
            Annotation.TOKEN to listOf("word", "token", "literal", "term", "form"),
            Annotation.LEMMA to listOf("lemma"),
            Annotation.POS to listOf("pos", "xpos"),
            Annotation.UPOS to listOf("upos"),
            Annotation.DEPREL to listOf("deprel"),
            Annotation.HEAD to listOf("head"),
            Annotation.NER to listOf("entity", "ner", "named-entity", "NamedEntity"),
        )
    }
}