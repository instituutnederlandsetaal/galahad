package org.ivdnt.galahad.formats.tei.export

import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerMerger
import org.ivdnt.galahad.export.LayerTransformer
import org.ivdnt.galahad.formats.tei.TeiFile
import org.ivdnt.galahad.formats.tei.TeiMetadata
import org.ivdnt.galahad.formats.xml.BLFXMLParser
import org.ivdnt.galahad.util.getXmlBuilder
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.OutputStream
import kotlin.io.path.createTempDirectory


class TeiMerger(
    teiFile: TeiFile,
    transformMetadata: DocumentExport,
) : LayerMerger<TeiFile>, LayerTransformer(transformMetadata) {

    private var xmlDoc = getXmlBuilder().newDocument()
    private val sortedWordForms = result.wordForms.sortedBy { it.offset }
    private val wordFormIter = sortedWordForms.listIterator()

    private val deleteList = ArrayList<Node>()
    private var parser: BLFXMLParser

    override fun merge(): TeiFile {
        val result = createTempDirectory("teimerge").toFile().resolve(document.name)
        result.writeText(parser.xmlToString(false))
        return TeiFile(result, document.metadata.format)
    }

    init {
        parser = BLFXMLParser.forFileWithFormat(
            transformMetadata.document.metadata.format,
            teiFile.file,
            OutputStream.nullOutputStream()
        ) { node: Node, offset: Int, document: Document ->
            val merger = TEITextMerger(
                node,
                offset,
                document,
                wordFormIter,
                deleteList,
                result,
                transformMetadata.document.metadata.format
            )
            merger.merge()
        }
        xmlDoc = parser.xmlDocument

        // add headers
        // typically we expect just 1 root node.
        for (i in 0 until parser.rootNodes.length) {
            TeiMetadata(xmlDoc, parser.rootNodes.item(i), this, merging = true)
        }

        // Delete the marked notes
        deleteList.forEach { if (it.parentNode != null) it.parentNode.removeChild(it) }
    }

}