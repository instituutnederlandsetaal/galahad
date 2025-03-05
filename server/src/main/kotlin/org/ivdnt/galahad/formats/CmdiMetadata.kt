package org.ivdnt.galahad.formats

import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.data.corpus.CorpusMetadata
import org.ivdnt.galahad.data.corpus.MutableCorpusMetadata
import org.ivdnt.galahad.tagset.TagsetStore
import org.ivdnt.galahad.util.escapeXML
import org.ivdnt.galahad.util.getXmlBuilder
import org.ivdnt.galahad.util.toNonEmptyString
import org.w3c.dom.Node
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.io.path.createTempDirectory

/** Constructs a CMDI file for exported documents. */
class CmdiMetadata(transformMetadata: DocumentTransformMetadata) : LayerTransformer(transformMetadata) {

    companion object {
        private val tmp_dir: File = createTempDirectory("cmdi").toFile()
    }

    /** We need tagsets to go from tagger.tagset to tagset.fullName */
    private val tagsets = TagsetStore()

    // Some vals for repeated access.
    private val docTitle = document.uploadedFile.nameWithoutExtension
    private val corpusMetadata: MutableCorpusMetadata = transformMetadata.corpus.mutableMetadata
    private val format = transformMetadata.targetFormat.identifier

    /** After initialization this file will contain the CMDI */
    val file: File

    init {
        // Load CMDI template
        val cmdiTemplate = this::class.java.classLoader.getResourceAsStream("CMDI-template.xml")
        val xmlDoc = getXmlBuilder().parse(cmdiTemplate)

        val replacements: Map<List<String>, String> = getReplacements()
        // Replace them
        for ((keys, value) in replacements) {
            val xpath = XPathFactory.newInstance().newXPath()
            for (key in keys) {
                val expr = xpath.compile("CMD//$key")
                val node = expr.evaluate(xmlDoc, XPathConstants.NODE) as Node
                node.textContent = value.escapeXML()
            }
        }
        // Write to disk
        file = tmp_dir.resolve("CMDI-$docTitle.xml")
        val tf: Transformer = TransformerFactory.newInstance().newTransformer()
        tf.transform(DOMSource(xmlDoc), StreamResult(file.outputStream()))
    }

    private fun getReplacements(): Map<List<String>, String> {
        // Current year, month and day, zero-padded
        val now = Date()
        val year = SimpleDateFormat("yyyy").format(now)
        val month = SimpleDateFormat("MM").format(now)
        val day = SimpleDateFormat("dd").format(now)
        val date = "$year-$month-$day"
        val galahadVersion = Config.galahadVersion()
        val uuid = document.metadata.uuid

        // Define replacements
        return mapOf(
            listOf("MdCollectionDisplayName", "corpusName") to corpusMetadata.name,
            listOf("MdCreationDate") to date,
            listOf("Annotation_GaLAHaD//yearFrom", "Annotation_GaLAHaD//yearTo") to year,
            listOf("Annotation_GaLAHaD//monthFrom", "Annotation_GaLAHaD//monthTo") to "--$month",
            listOf("Annotation_GaLAHaD//dayFrom", "Annotation_GaLAHaD//dayTo") to "---$day",
            listOf("ResourceRef") to "https://resolver.ivdnt.org/$uuid",
            listOf("GaLAHaDPersistentIdentifier") to "${uuid}_tei",
            listOf("conversionDescription") to "exported to $format by GaLAHaD",
            listOf("Conversion_GaLAHaD//toolVersion") to galahadVersion,
            listOf("sourceID") to docTitle,
            listOf("sourceCollection") to corpusMetadata.sourceName.toNonEmptyString("!No source name defined!"),
            listOf("sourceCollectionURI") to corpusMetadata.sourceURL.toNonEmptyString("!No source URL defined!"),
            listOf("Source_GaLAHaD//yearFrom") to corpusMetadata.eraFrom.toString(),
            listOf("Source_GaLAHaD//yearTo") to corpusMetadata.eraTo.toString(),
            listOf("languageName") to corpusMetadata.language.toNonEmptyString("Dutch"),
            listOf("annotationSet") to tagsets.getOrNull(tagger.tagset)?.longName.toNonEmptyString("!No tagset defined!"),
            listOf("annotationFormat") to format,
            listOf("Annotation_GaLAHaD//toolName") to tagger.id,
            listOf("Annotation_GaLAHaD//toolVersion") to tagger.version,
            listOf("Annotation_GaLAHaD//toolURL") to tagger.model.href,
        )
    }
}