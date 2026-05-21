package org.ivdnt.galahad.export

import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.util.*
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Constructs a CMDI file for the exported document. Uses a template CMDI file (in resources/) and
 * fills in the metadata. That file only needs to be parsed once and can be reused, simply
 * filling/overwriting the metadata values in the DOM with each instance.
 */
class CmdiMetadata(val export: DocumentExport) {
    private val docTitle = export.document.sourceFile.withoutFormatExt
    private val corpus = export.corpus.metadata
    private val format = export.format.identifier
    private val now = Date()
    private val year = SimpleDateFormat("yyyy").format(now)
    private val month = SimpleDateFormat("MM").format(now)
    private val day = SimpleDateFormat("dd").format(now)
    private val date = "$year-$month-$day"
    private val uuid = export.layer.id
    private val tagger = export.layers.metadata.tagger
    private val tagset = tagger.principles.ifNullOrBlank { "!No tagset defined!" }
    private val language = corpus.language.ifNullOrBlank { "Dutch" }
    private val sourceName = corpus.source?.name?.ifNullOrBlank { "!No source name defined!" }
    private val sourceUrl =
        corpus.source?.url?.toString().ifNullOrBlank { "!No source URL defined!" }

    /** Write the CMDI file to the given [out]put stream. */
    fun write(out: OutputStream) {
        writeCmdHeader()
        writeCmdResources()
        writeCmdComponents()
        TransformerFactory.newInstance()
            .newTransformer()
            .transform(DOMSource(xml), StreamResult(out))
    }

    /**
     * Write to CMDI Header.
     *
     * ```xml
     * <cmd:Header>
     *     <cmd:MdCreationDate>DATE</cmd:MdCreationDate>
     *     <cmd:MdCollectionDisplayName>CORPUS_NAME</cmd:MdCollectionDisplayName>
     * </cmd:Header>
     * ```
     */
    private fun writeCmdHeader() {
        val header = root.child("cmd:Header")
        header.child("cmd:MdCreationDate").textContent = date
        header.child("cmd:MdCollectionDisplayName").textContent = corpus.name
    }

    /**
     * Write to CMDI Resources.
     *
     * ```xml
     * <cmd:Resources>
     *     <cmd:ResourceProxyList>
     *         <cmd:ResourceProxy id="landingpage">
     *             <cmd:ResourceRef>https://resolver.ivdnt.org/PID_tei</cmd:ResourceRef>
     *         </cmd:ResourceProxy>
     *     </cmd:ResourceProxyList>
     * </cmd:Resources>
     * ```
     */
    private fun writeCmdResources() {
        val resources = root.child("cmd:Resources")
        val resourceProxyList = resources.child("cmd:ResourceProxyList")
        val resourceProxy = resourceProxyList.child("cmd:ResourceProxy")
        resourceProxy.child("cmd:ResourceRef").textContent = "https://resolver.ivdnt.org/$uuid"
    }

    /**
     * Write to CMDI Components.
     *
     * ```xml
     * <cmd:Components>
     *     <cmdp:TextProfileINT_GaLAHaD_v4>
     *         <cmdp:corpusName>CORPUS_NAME</cmdp:corpusName>
     *         <cmdp:TextFile_GaLAHaD .../>
     *         <cmdp:Source_GaLAHaD .../>
     *         <cmdp:Language_GaLAHaD>
     *             <cmdp:languageName>LANGUAGE</cmdp:languageName>
     *         </cmdp:Language_GaLAHaD>
     *         <cmdp:Annotation_GaLAHaD .../>
     *     </cmdp:TextProfileINT_GaLAHaD_v4>
     * </cmd:Components>
     */
    private fun writeCmdComponents() {
        val components = root.child("cmd:Components").childElements.first()
        // Components.corpusName
        components.child("cmdp:corpusName").textContent = corpus.name
        // Components.TextFile_GaLAHaD
        writeCmdComponentsTextFile(components)
        // Components.Source_GaLAHaD
        writeCmdComponentsSource(components)
        // Components.Language_GaLAHaD
        components.child("cmdp:Language_GaLAHaD").child("cmdp:languageName").textContent = language
        // Components.Annotation_GaLAHaD
        writeCmdComponentsAnnotation(components)
    }

    /**
     * Write to CMDI Components.TextFile_GaLAHaD.
     *
     * ```xml
     * <cmdp:TextFile_GaLAHaD>
     *     <cmdp:GaLAHaDPersistentIdentifier>PID_tei</cmdp:GaLAHaDPersistentIdentifier>
     *     <cmdp:Conversion_GaLAHaD>
     *         <cmdp:conversionDescription>exported as FORMAT by GaLAHaD</cmdp:conversionDescription>
     *         <cmdp:Tool>
     *             <cmdp:toolVersion>VERSION</cmdp:toolVersion>
     *         </cmdp:Tool>
     *     </cmdp:Conversion_GaLAHaD>
     * </cmdp:TextFile_GaLAHaD>
     */
    private fun writeCmdComponentsTextFile(components: Element) {
        val textFileGalahad = components.child("cmdp:TextFile_GaLAHaD")
        textFileGalahad.child("cmdp:GaLAHaDPersistentIdentifier").textContent = "${uuid}_$format"
        // Components.TextFile_GaLAHaD.Conversion_GaLAHaD
        val conversionGalahad = textFileGalahad.child("cmdp:Conversion_GaLAHaD")
        conversionGalahad.child("cmdp:conversionDescription").textContent =
            "exported as $format by GaLAHaD"
        conversionGalahad.child("cmdp:Tool").child("cmdp:toolVersion").textContent = version
    }

    /**
     * Write to CMDI Components.Source_GaLAHaD.
     *
     * ```xml
     * <cmdp:Source_GaLAHaD>
     *     <cmdp:sourceID>TITLE</cmdp:sourceID>
     *     <cmdp:sourceCollection>SOURCE_NAME</cmdp:sourceCollection>
     *     <cmdp:sourceCollectionURI>SOURCE_URL</cmdp:sourceCollectionURI>
     *     <cmdp:Date_Period>
     *         <cmdp:yearFrom>ERA_FROM</cmdp:yearFrom>
     *         <cmdp:yearTo>ERA_TO</cmdp:yearTo>
     *     </cmdp:Date_Period>
     * </cmdp:Source_GaLAHaD>
     */
    private fun writeCmdComponentsSource(components: Element) {
        val sourceGalahad = components.child("cmdp:Source_GaLAHaD")
        sourceGalahad.child("cmdp:sourceID").textContent = docTitle
        sourceGalahad.child("cmdp:sourceCollection").textContent = sourceName
        sourceGalahad.child("cmdp:sourceCollectionURI").textContent = sourceUrl
        // Components.Source_GaLAHaD.Date_Period
        sourceGalahad.child("cmdp:Date_Period").apply {
            child("cmdp:yearFrom").textContent = "${corpus.period?.from}"
            child("cmdp:yearTo").textContent = "${corpus.period?.to}"
        }
    }

    /**
     * Write to CMDI Components.Annotation_GaLAHaD.
     *
     * ```xml
     * <cmdp:Annotation_GaLAHaD>
     *     <cmdp:annotationSet>TAGSET</cmdp:annotationSet>
     *     <cmdp:Provenance>
     *         <cmdp:annotationFormat>FORMAT</cmdp:annotationFormat>
     *         <cmdp:AnnotationProcess .../>
     *     </cmdp:Provenance>
     * </cmdp:Annotation_GaLAHaD>
     * ```
     */
    private fun writeCmdComponentsAnnotation(components: Element) {
        val annotationGalahad = components.child("cmdp:Annotation_GaLAHaD")
        annotationGalahad.child("cmdp:annotationSet").textContent = tagset
        // Components.Annotation_GaLAHaD.Provenance
        val provenance = annotationGalahad.child("cmdp:Provenance")
        provenance.child("cmdp:annotationFormat").textContent = format
        // Components.Annotation_GaLAHaD.Provenance.AnnotationProcess
        val annotationProcess = provenance.child("cmdp:AnnotationProcess")
        writeCmdComponentsAnnotationProcess(annotationProcess)
    }

    /**
     * Write to CMDI Components.Annotation_GaLAHaD.Provenance.AnnotationProcess.
     *
     * ```xml
     * <cmdp:AnnotationProcess>
     *     <cmdp:ProcessorsAnnotators>
     *         <cmdp:Tool>
     *             <cmdp:toolName>TAGGER_NAME</cmdp:toolName>
     *             <cmdp:toolVersion>TAGGER_VERSION</cmdp:toolVersion>
     *             <cmdp:toolURL>TAGGER_URL</cmdp:toolURL>
     *         </cmdp:Tool>
     *     </cmdp:ProcessorsAnnotators>
     *     <cmdp:Date_Period>
     *         <cmdp:yearFrom>YEAR</cmdp:yearFrom>
     *         <cmdp:yearTo>YEAR</cmdp:yearTo>
     *         <cmdp:monthFrom>--MONTH</cmdp:monthFrom>
     *         <cmdp:monthTo>--MONTH</cmdp:monthTo>
     *         <cmdp:dayFrom>---DAY</cmdp:dayFrom>
     *         <cmdp:dayTo>---DAY</cmdp:dayTo>
     *     </cmdp:Date_Period>
     * </cmdp:AnnotationProcess>
     * ```
     */
    private fun writeCmdComponentsAnnotationProcess(annotationProcess: Node) {
        // Components.Annotation_GaLAHaD.Provenance.AnnotationProcess.ProcessorsAnnotators.Tool
        annotationProcess.child("cmdp:ProcessorsAnnotators").child("cmdp:Tool").apply {
            child("cmdp:toolName").textContent = tagger.name
            // child("cmdp:toolVersion").textContent = tagger.version
            // child("cmdp:toolURL").textContent = tagger.uri
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
    }

    companion object {
        // Load CMDI template
        private val cmdiTemplate =
            CmdiMetadata::class.java.classLoader.getResourceAsStream("CMDI-template.xml")
        private val xml = XmlUtil.builder.parse(cmdiTemplate)
        private val root = xml.documentElement

        // This reads a file from disk, so only do it once.
        private val version: String = Config.galahadVersion
    }
}
