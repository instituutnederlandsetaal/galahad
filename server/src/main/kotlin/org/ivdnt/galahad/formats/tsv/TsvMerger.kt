package org.ivdnt.galahad.formats.tsv

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerMerger
import java.io.OutputStream
import java.io.PrintWriter

open class TsvMerger(
    export: DocumentExport,
) : LayerMerger(export) {
    protected open val columnIndices: MutableMap<Annotation, Int> = mutableMapOf()
    override fun merge(out: OutputStream): Unit = merge(PrintWriter(out.bufferedWriter()))
    protected var termIndex: Int = 0

    /**
     * Merge uploaded raw file with tagger layer. Headers indices are already determined by TSVFile.
     * Read in per line, split on tabs, swap out pos & lemma and commit to new file
     */
    fun merge(out: PrintWriter) {
        export.document.uploadedFile.forEachLine { line ->
            if (columnIndices.isEmpty()) {
                getColumnIndices(line.split("\t"))
            } else {
                if (line.isBlank()) {
                    out.println()
                }
                val columns = line.split("\t").toMutableList()
                // Swap out pos & lemma, keep the rest.
                replaceColumns(columns)
                out.println(columns.joinToString("\t") + "\n")
                termIndex++
            }
        }
    }

    private fun getColumnIndices(
        headers: List<String>,
    ) {
        headers.forEachIndexed { index, header ->
            TsvReader.columnNames.entries
                // from the columnNames, find the first AnnotationType that has a name that matches the header.
                .firstOrNull { (_, names) ->
                    names.any { name -> header.equals(name, ignoreCase = true) }
                    // if it exists, register the index
                }?.let { (annotation, _) ->
                    columnIndices[annotation] = index
                }
        }
    }

    /*
     * Replace annotations in their previously indexed columns.
     */
    private fun replaceColumns(
        columns: MutableList<String>,
    ) {
        export.tagger.annotations.forEach { annot ->
            val index = columnIndices[annot] ?: return@forEach // Skip if not in the file.
            mergeSingleColumn(columns, annot, index)
        }
    }

    protected open fun mergeSingleColumn(
        columns: MutableList<String>,
        annotation: Annotation,
        columnIndex: Int,
    ) {
        val term = termComparisons[termIndex].hypoTerm
        columns[columnIndex] = term.annotationOrMissing(annotation)
    }
}