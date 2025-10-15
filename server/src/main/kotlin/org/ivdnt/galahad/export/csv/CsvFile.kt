package org.ivdnt.galahad.export.csv

import java.io.File

typealias CsvString = String

class CsvFile(
    path: File,
) : File(path.toURI()) {

    init {
        this.append(EXCEL_HEADER)
    }

    /** Append Excel compatible text. */
    fun append(text: CsvString) {
        this.appendText(text, Charsets.UTF_16LE)
    }

    companion object {
        // Alternatively we could check for forbidden characters first, and the wrap/replace only when necessary.
        // However, this works and gives a consistent result
        private fun csvEscape(s: String): String = "\"${s.replace("\"", "\"\"")}\""

        // BOM forces Excel to read UTF16LE. Needed for e.g. 'ü'. (https://en.wikipedia.org/wiki/Byte_order_mark)
        // Explicit separator needed as default will be ',' in the US but ';' in EU.
        private const val EXCEL_HEADER: String = "${'\uFEFF'}sep=,\n"

        fun toCsvString(values: List<Any>): CsvString =
            values.joinToString(",") { csvEscape(it.toString()) }.plus("\n")
    }
}