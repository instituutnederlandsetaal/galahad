package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.formats.tei.export.TEILayerMerger
import org.ivdnt.galahad.formats.xml.BLFXMLParser
import org.ivdnt.galahad.util.getXmlBuilder
import java.io.File

class TEIFile(
    override val file: File,
    override val format: DocumentFormat,
) : InternalFile {
    override val plaintext: String by lazy { parse(); plainTextFile.readText() }
    override val sourceLayer: Layer by lazy { parse(); _sourceLayer }

    private var _sourceLayer: Layer = Layer.EMPTY

    private var isParsed = false

    private val plainTextFile = File.createTempFile("galahad-${file.name}-plaintext", ".txt")

    constructor(file: File) : this(file, DocumentFormat.fromFile(file))

    fun parse() {
        if (isParsed) return // Don't double parse
        isParsed = true

        val xmlDocument = getXmlBuilder().parse(file)
        val xmlParser = BLFXMLParser.forFormat(format, plainTextFile.outputStream(), xmlDocument) { _, _, _ -> }
        _sourceLayer = xmlParser.sourceLayer
    }

    override fun merge(transformMetadata: DocumentTransformMetadata): TEIFile =
        TEILayerMerger(this, transformMetadata).merge()
}