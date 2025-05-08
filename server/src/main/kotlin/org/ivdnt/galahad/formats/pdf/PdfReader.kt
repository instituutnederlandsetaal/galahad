package org.ivdnt.galahad.formats.pdf

import com.itextpdf.text.pdf.parser.PdfTextExtractor
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.AnnotationReader
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import java.io.InputStream
import com.itextpdf.text.pdf.PdfReader as PdfReaderIText

class PdfReader(
    stream: InputStream
) : AnnotationReader() {
    val reader = PdfReaderIText(stream)

    override fun read(): Layer {
        for (i in 1..reader.numberOfPages) {
            val text = PdfTextExtractor.getTextFromPage(reader, i)
            text.ifBlank { null }?.split(whitespace)?.filter { it.isNotBlank() }?.forEach { word ->
                terms += Term(
                    wordID(), offset, mapOf(
                        Annotation.TOKEN to word
                    )
                )
                offset += word.length + 1
            }
        }
        newDocument()
        return Layer(documents.toTypedArray())
    }

    companion object {
        val whitespace: Regex = Regex("""\s+""")
    }

}