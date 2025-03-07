package org.ivdnt.galahad.formats.folia.export

import org.ivdnt.galahad.formats.DocumentExport
import org.ivdnt.galahad.formats.LayerMerger
import org.ivdnt.galahad.formats.LayerTransformer
import org.ivdnt.galahad.formats.folia.FoliaFile
import org.ivdnt.galahad.formats.folia.FoliaMetadata
import org.ivdnt.galahad.formats.folia.FoliaReader
import org.ivdnt.galahad.formats.xml.BLFXMLParser
import org.w3c.dom.Document
import org.w3c.dom.Node
import kotlin.io.path.createTempDirectory

class FoliaLayerMerger(
    foliaFile: FoliaFile,
    transformMetadata: DocumentExport,
) : LayerMerger<FoliaFile>, LayerTransformer(transformMetadata) {

    private val sortedWordForms = result.wordForms.sortedBy { it.offset }
    private val wordFormIter = sortedWordForms.listIterator()
    private val deleteList = ArrayList<Node>()
    private var reader: FoliaReader? = null

    init {
        reader = FoliaReader(foliaFile.file) { node: Node, offset: Int, document: Document ->
            val merger = FoliaTextMerger(node, offset, document, wordFormIter, deleteList, transformMetadata.layer)
            merger.merge()
        }
        reader!!.read()

        // add headers
        // typically we expect just 1 root node.
        FoliaMetadata(reader!!.xmlDoc, reader!!.xmlDoc.documentElement, this).write()

        // Delete the marked notes
        deleteList.forEach { if (it.parentNode != null) it.parentNode.removeChild(it) }
    }

    override fun merge(): FoliaFile {
        val result = createTempDirectory("foliamerge").toFile().resolve(document.name)
        result.writeText(BLFXMLParser.xmlToString(false, reader!!.xmlDoc))
        return FoliaFile(result)
    }
}
