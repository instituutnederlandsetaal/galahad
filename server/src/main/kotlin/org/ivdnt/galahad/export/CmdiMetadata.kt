package org.ivdnt.galahad.export

import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.corpora.MutableCorpusMetadata
import org.ivdnt.galahad.taggers.Tagset
import org.ivdnt.galahad.util.XmlUtil
import org.ivdnt.galahad.util.child
import org.ivdnt.galahad.util.childElements
import org.ivdnt.galahad.util.ifNullOrBlank
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.io.path.createTempDirectory

/** Constructs a CMDI file for exported documents. */
class CmdiMetadata(val export: DocumentExport) {

    // Some vals for repeated access.
    private val docTitle = export.document.uploadedFile.nameWithoutExtension
    private val corpus: MutableCorpusMetadata = export.corpus.mutableMetadata
    private val format = export.format.identifier
    private val now = Date()
    private val year = SimpleDateFormat("yyyy").format(now)
    private val month = SimpleDateFormat("MM").format(now)
    private val day = SimpleDateFormat("dd").format(now)
    private val date = "$year-$month-$day"
    private val uuid = export.document.metadata.uuid
    private val tagset = Tagset.readOrNull(export.tagger)?.longName.ifNullOrBlank { "!No tagset defined!" }
    private val tagger = export.tagger
    private val language = corpus.language.ifNullOrBlank { "Dutch" }
    private val sourceName = corpus.sourceName.ifNullOrBlank { "!No source name defined!" }
    private val sourceUrl = corpus.sourceURL?.toString().ifNullOrBlank { "!No source URL defined!" }

    fun write(out: OutputStream) {
        // Header
        val header = root.child("cmd:Header")
        header.child("cmd:MdCreationDate").textContent = date
        header.child("cmd:MdCollectionDisplayName").textContent = corpus.name
        // Resources
        val resources = root.child("cmd:Resources")
        val resourceProxyList = resources.child("cmd:ResourceProxyList")
        val resourceProxy = resourceProxyList.child("cmd:ResourceProxy")
        resourceProxy.child("cmd:ResourceRef").textContent = "https://resolver.ivdnt.org/$uuid"
        // Components
        val components = root.child("cmd:Components").childElements.first()
        // Components.corpusName
        components.child("cmdp:corpusName").textContent = corpus.name
        // Components.TextFile_GaLAHaD
        val textFileGalahad = components.child("cmdp:TextFile_GaLAHaD")
        textFileGalahad.child("cmdp:GaLAHaDPersistentIdentifier").textContent = "${uuid}_$format"
        // Components.TextFile_GaLAHaD.Conversion_GaLAHaD
        val conversionGalahad = textFileGalahad.child("cmdp:Conversion_GaLAHaD")
        conversionGalahad.child("cmdp:conversionDescription").textContent = "exported to $format by GaLAHaD"
        conversionGalahad.child("cmdp:Tool").child("cmdp:toolVersion").textContent = version
        // Components.Source_GaLAHaD
        val sourceGalahad = components.child("cmdp:Source_GaLAHaD")
        sourceGalahad.child("cmdp:sourceID").textContent = docTitle
        sourceGalahad.child("cmdp:sourceCollection").textContent = sourceName
        sourceGalahad.child("cmdp:sourceCollectionURI").textContent = sourceUrl
        // Components.Source_GaLAHaD.Date_Period
        sourceGalahad.child("cmdp:Date_Period").apply {
            child("cmdp:yearFrom").textContent = "${corpus.eraFrom}"
            child("cmdp:yearTo").textContent = "${corpus.eraTo}"
        }
        // Components.Language_GaLAHaD
        components.child("cmdp:Language_GaLAHaD").child("cmdp:languageName").textContent = language
        // Components.Annotation_GaLAHaD
        val annotationGalahad = components.child("cmdp:Annotation_GaLAHaD")
        annotationGalahad.child("cmdp:annotationSet").textContent = tagset
        // Components.Annotation_GaLAHaD.Provenance
        val provenance = annotationGalahad.child("cmdp:Provenance")
        provenance.child("cmdp:annotationFormat").textContent = format
        // Components.Annotation_GaLAHaD.Provenance.AnnotationProcess
        val annotationProcess = provenance.child("cmdp:AnnotationProcess")
        // Components.Annotation_GaLAHaD.Provenance.AnnotationProcess.ProcessorsAnnotators.Tool
        annotationProcess.child("cmdp:ProcessorsAnnotators").child("cmdp:Tool").apply {
            child("cmdp:toolName").textContent = tagger.id
            child("cmdp:toolVersion").textContent = tagger.version
            child("cmdp:toolURL").textContent = tagger.model.href
        }
        // Components.Annotation_GaLAHaD.Provenance.AnnotationProcess.Date_Period
        annotationProcess.child("cmdp:Date_Period").apply {
            child("cmdp:yearFrom").textContent = year
            child("cmdp:yearTo").textContent = year
            child("cmdp:monthFrom").textContent = "--$month"
            child("cmdp:monthTo").textContent = "--$month"
            child("cmdp:dayFrom").textContent = "---$day"
            child("cmdp:dayTo").textContent = "---$day"
        }
        // Write to disk
        XmlUtil.transformer.transform(DOMSource(xml), StreamResult(out))
    }

    companion object {
        // Load CMDI template
        private val cmdiTemplate = CmdiMetadata::class.java.classLoader.getResourceAsStream("CMDI-template.xml")
        private val xml = XmlUtil.builder.parse(cmdiTemplate)
        private val root = xml.documentElement
        private val tmp_dir: File = createTempDirectory("cmdi").toFile()
        // This read a file from disk, so only do it once.
        private val version: String = Config.galahadVersion
    }
}