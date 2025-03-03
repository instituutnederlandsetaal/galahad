package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.data.layer.Annotations
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.token
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.formats.conllu.ConlluFile
import org.ivdnt.galahad.formats.tsv.export.TSVLayerMerger
import java.io.File
import java.io.FileOutputStream
import java.io.Reader

open class TSVFile(
    override val file: File,
) : InternalFile {

    private val plainTextFile = File.createTempFile("galahad-${file.name}-plaintext", ".txt")
    override val format: DocumentFormat = DocumentFormat.Tsv
    val entries = ArrayList<Annotations>()
    private var sourceLayer: Layer = Layer.EMPTY
    private var isParsed: Boolean = false

    open val columnIndices: MutableMap<AnnotationType, Int> = mutableMapOf()

    val columnNames: Map<AnnotationType, List<String>> = mapOf(
        AnnotationType.TOKEN to listOf("word", "token", "literal", "term", "form"),
        AnnotationType.LEMMA to listOf("lemma"),
        AnnotationType.POS to listOf("pos", "xpos"),
        AnnotationType.UPOS to listOf("upos"),
        AnnotationType.DEPREL to listOf("deprel"),
        AnnotationType.HEAD to listOf("head"),
        AnnotationType.ID to listOf("id"),
        AnnotationType.NER to listOf("entity", "ner", "named-entity", "NamedEntity"),
    )

    override fun plainTextReader(): Reader {
        if (!isParsed) parse()
        return plainTextFile.reader()
    }

    override fun sourceLayer(): Layer {
        if (!isParsed) parse()
        return sourceLayer
    }

    /**
     * Generate plaintext, sourcelayer and retrieve indices for literal, lemma and PoS.
     */
    fun parse() {
        if (isParsed) return // Don't double parse
        isParsed = true

        // Generate plaintext
        val stream = plainTextFile.outputStream()
        for ((i, line) in file.readLines().withIndex()) {
            if (i == 0 && this !is ConlluFile) {
                parseHeader(line) // Also retrieves indices.
            } else {
                parseBody(line, stream)
            }
        }
        stream.close()
        // Generate sourcelayer
        sourceLayer = mapOnPlainText(plainTextFile.readText(), SOURCE_LAYER_NAME)
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
        if (columnIndices[AnnotationType.TOKEN] == null) {
            throw IllegalArgumentException("No token column found in TSV file.")
        }
    }

    // Derived classes may want to look for other names or indices.
    /**
     * Set up columnIndices to reflect the header.
     * For each header column, check if it is the name of any of the AnnotationTypes.
     */
    protected open fun getColumnIndices(
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

    private fun parseBody(line: String, stream: FileOutputStream) {
        // Split on tabs
        val values: List<String> = line.split("\t")

        // Retrieve values
        val mutAnnot: MutableMap<AnnotationType, String?> = mutableMapOf()
        for (column in columnIndices.entries) {
            getColumn(column.value, values)?.let { mutAnnot[column.key] = it }
        }

        // Skip newlines by checking for non-empty literals.
        if (mutAnnot[AnnotationType.TOKEN] != null && values.size >= 2) {
            val annotations: Annotations = mutAnnot
            // Commit values if non-empty
            entries.add(annotations)
            // Write to plaintext
            stream.write("${annotations.token} ".toByteArray()) // Note space between words.
        } else {
            stream.write("\n".toByteArray())
        }
    }

    // Retrieves a column with bounds checking.
    protected open fun getColumn(index: Int?, values: List<String>): String? {
        return if (index != null && index < values.size) {
            val value = values[index]
            value.ifBlank {
                null
            }
        } else {
            null
        }
    }

    // Derived classes may have to construct a PoS differently.
    //protected open fun getPos(values: List<String>): String? = getColumn(posIndex!!, values)

    /**
     * Assumes the TSV file is mappable onto the provided plaintext
     */
    fun mapOnPlainText(plaintext: String, layerName: String): Layer {
        parse() // Parse in order to obtain this.entries.
        val newLayer = Layer(layerName)
        var index = 0

        // We use this variable to skip ahead after a match
        var lastPlainOffset = 0

        for (i in 0..plaintext.length) {
            if (i >= lastPlainOffset) {
                if (index < entries.size) {
                    val literal = entries[index].token
                    if (i + literal.length <= plaintext.length) {
                        val candidate = plaintext.substring(i until i + literal.length)
                        if (literal == candidate) {
                            // Match!!
                            newLayer.addTSVEntryOnOffset(entries[index], i)
                            lastPlainOffset = i + literal.length
                            index++
                        }
                    }
                }
            }
        }
        return newLayer
    }

    override fun merge(transformMetadata: DocumentTransformMetadata): TSVFile {
        // Sets header indices needed to merge.
        parse()
        return TSVLayerMerger(this, transformMetadata).merge()
    }
}