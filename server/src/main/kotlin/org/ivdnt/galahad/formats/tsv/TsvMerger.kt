package org.ivdnt.galahad.formats.tsv

import java.io.OutputStream
import java.io.PrintWriter
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerMerger

open class TsvMerger(export: DocumentExport) : LayerMerger(export) {
    protected open val columnIndices: MutableMap<Annotation, Int> = mutableMapOf()

    override fun merge(out: OutputStream): Unit = merge(PrintWriter(out))

    protected var termIndex: Int = 0
    private var extraColumns: MutableList<Annotation> = mutableListOf()
    protected open val emptyValue: String = ""

    /**
     * Merge uploaded raw file with tagger layer. Headers indices are already determined by TSVFile.
     * Read in per line, split on tabs, swap out pos & lemma and commit to new file
     */
    fun merge(out: PrintWriter) {
        export.sourceDocument.sourceFile.forEachLine { line ->
            if (columnIndices.isEmpty()) {
                val headers = line.split("\t")
                getColumnIndices(headers)
                // Print header with any extra columns
                if (columnIndices.isNotEmpty()) {
                    out.println((headers + extraColumns).joinToString("\t"))
                }
            } else if (!line.startsWith("#") && line.isNotBlank()) {
                val columns = line.split("\t").toMutableList()
                // Add extra columns.
                columns.addAll(List(extraColumns.size) { "" })
                // Swap out merging annotations, keep the rest.
                replaceColumns(columns)
                out.println(columns.joinToString("\t"))
                termIndex++
            } else {
                out.println(line)
            }
        }
        out.flush()
    }

    private fun getColumnIndices(headers: List<String>) {
        headers.forEachIndexed { index, header ->
            TsvReader.columnNames.entries
                // from the columnNames, find the first AnnotationType that has a name that matches
                // the
                // header.
                .firstOrNull { (_, names) ->
                    names.any { name -> header.equals(name, ignoreCase = true) }
                    // if it exists, register the index
                }
                ?.let { (annotation, _) -> columnIndices[annotation] = index }
        }
        if (headers.isEmpty()) return // This line was not yet the header.
        // Add any missing columns.
        export.document.metadata.annotations.keys.forEach { annotation ->
            if (columnIndices[annotation] == null) {
                columnIndices[annotation] = headers.size + extraColumns.size
                extraColumns.add(annotation)
            }
        }
    }

    /*
     * Replace annotations in their previously indexed columns.
     */
    protected open fun replaceColumns(columns: MutableList<String>) {
        export.document.metadata.annotations.keys
            .filter { it != Annotation.TOKEN }
            .forEach { annot ->
                val index = columnIndices[annot] ?: return@forEach // Skip if not in the file.
                mergeSingleColumn(columns, annot, index)
            }
    }

    protected open fun mergeSingleColumn(
        columns: MutableList<String>,
        annotation: Annotation,
        columnIndex: Int,
    ) {
        val term = termComparisons[termIndex].hyp
        columns[columnIndex] = term.annotations[annotation] ?: emptyValue
    }
}
