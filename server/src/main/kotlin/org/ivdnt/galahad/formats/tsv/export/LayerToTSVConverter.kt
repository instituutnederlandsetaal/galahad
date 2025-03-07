package org.ivdnt.galahad.formats.tsv.export

import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.DocumentExport
import org.ivdnt.galahad.formats.LayerConverter
import org.ivdnt.galahad.formats.LayerTransformer
import java.io.OutputStream

class LayerToTSVConverter(
    transformMetadata: DocumentExport,
) : LayerConverter, LayerTransformer(transformMetadata) {

    override val format: DocumentFormat
        get() = DocumentFormat.Tsv

    override fun convert(outputStream: OutputStream) {
        // Header
        outputStream.write("word\tlemma\tpos\n".encodeToByteArray()) // 'word' is the blacklab default
        // Body
        result.terms.forEach {
            // Explicitly non-null.
            outputStream.write("${it.literals}\t${it.lemmaOrEmpty}\t${it.posOrEmpty}\n".encodeToByteArray())
        }
    }
}