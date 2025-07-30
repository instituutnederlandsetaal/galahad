package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.corpora.MutableCorpusMetadata
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.util.XmlUtil
import org.ivdnt.galahad.util.childOrNull
import org.ivdnt.galahad.util.ifNullOrBlank
import org.ivdnt.galahad.util.withoutFormatExt
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.text.SimpleDateFormat

class TeiMetadata(
    xml: Document,
    val root: Node,
    val export: DocumentExport,
    val merging: Boolean,
) : XmlUtil(xml) {

    /** GaLAHaD-generated UUID */
    private val internalPid: String = export.layer.id

    /**
     * Return the title of the document as described in titleStmt,
     * or the filename without extension if the former is missing.
     */
    private val title: String
        get() {
            return root.childOrNull("teiHeader")
                ?.childOrNull("fileDesc")
                ?.childOrNull("titleStmt")
                ?.childOrNull("title")?.textContent
                ?: // if null, use filename without extension
                export.document.uploadedFile.withoutFormatExt
        }

    private val corpusMetadata: MutableCorpusMetadata = export.corpus.mutableMetadata

    init {
        write()
    }

    // There could be multiple root nodes in the document.
    // The caller specifies which one.
    private fun write() {
        // Add namespace to root for LAnCeLoT compatibility
        // TODO can be removed?
        (root as Element).setAttribute("xmlns", "http://www.tei-c.org/ns/1.0")

        val teiHeader = root.getOrCreateChild("teiHeader", true)
        // remove namespace for Cobalt compatibility
        // TODO if this really needs to be removed, remove it in TeiMerger? although teiHeader could be null
        teiHeader.removeAttribute("xmlns")
        root.childOrNull("text")?.removeAttribute("xmlns")

        // Add metadata to the document
        addFileDescMetadata(teiHeader)
        addEncodingDescMetadata(teiHeader)
        addProfileDescMetadata(teiHeader)
    }

    /**
     * Add a file description to [teiHeader]:
     * <fileDesc>
     *     <titleStmt>...</titleStmt>
     *     <publicationStmt>...</publicationStmt>
     *     <notesStmt>...</notesStmt>
     *     <sourceDesc>...</sourceDesc>
     * </fileDesc>
     */
    private fun addFileDescMetadata(teiHeader: Element) {
        // <fileDesc>
        val fileDesc = teiHeader.getOrCreateChild("fileDesc")
        // <titleStmt>
        addTitleStmt(fileDesc)
        // <publicationStmt>
        addPublicationStmt(fileDesc)
        // <notesStmt>
        addNotesStmt(fileDesc)
        // <sourceDesc>
        addSourceDesc(fileDesc)
    }

    /**
     * Add title statement to [fileDesc]:
     * <titleStmt>
     *     <title>[title]</title>
     *     <respStmt>...</respStmt>
     *     <respStmt>...</respStmt>
     * </titleStmt>
     */
    private fun addTitleStmt(fileDesc: Element) {
        val titleStmt = fileDesc.getOrCreateChild("titleStmt")
        if (titleStmt.childOrNull("title") == null) {
            // Only add if not already present (when merging).
            titleStmt.createChild("title", title)
        }
        addRespStmt(titleStmt, "linguistic annotation by GaLAHaD (https://portal.clarin.ivdnt.org/galahad)")
        if (merging) {
            addRespStmt(titleStmt, "TEI merged by GaLAHaD (https://portal.clarin.ivdnt.org/galahad)")
        } else {
            addRespStmt(
                titleStmt,
                "exported as ${DocumentFormat.TeiP5.identifier} by GaLAHaD (https://portal.clarin.ivdnt.org/galahad)"
            )
        }
    }

    /**
     * Add a responsibility statement to [titleStmt]:
     * <respStmt>
     *     <resp>...</resp>
     *     <orgName xml:lang="nl">Instituut voor de Nederlandse Taal</orgName>
     *     <orgName xml:lang="en">Dutch Language Institute</orgName>
     * </respStmt>
     */
    private fun addRespStmt(titleStmt: Element, resp: String) {
        val respStmt = titleStmt.createChild("respStmt")
        respStmt.createChild("resp", resp)
        respStmt.createChild("orgName", "xml:lang" to "nl", "Instituut voor de Nederlandse Taal")
        respStmt.createChild("orgName", "xml:lang" to "en", "Dutch Language Institute")
    }

    /**
     * Add publication statement to [fileDesc]:
     * <publicationStmt>
     *     <publisher>!Needs to be filled in!</publisher>
     *     <idno type="sourceID">[title]</idno>
     *     <idno type="internalPersistentIdentifier">[internalPid]</idno>
     * </publicationStmt>
     */
    private fun addPublicationStmt(fileDesc: Element) {
        val publicationStmt = fileDesc.getOrCreateChild("publicationStmt")
        if (publicationStmt.childOrNull("publisher") == null) {
            // Only add if not already present (when merging).
            publicationStmt.createChild("publisher", "!Needs to be filled in!")
        }
        publicationStmt.createChild("idno", title, "sourceID")
        publicationStmt.createChild("idno", "${internalPid}_tei", "GaLAHaDPersistentIdentifier")
    }

    /**
     * Add notes statement to [fileDesc]:
     * <notesStmt>
     *     <note type="corpusName">[name]</note>
     *     <note type="sourceCollection">[sourceName]</note>
     *     <note type="sourceCollectionURL">[sourceURL]</note>
     * </notesStmt>
     */
    private fun addNotesStmt(fileDesc: Element) {
        val notesStmt = fileDesc.getOrCreateChild("notesStmt")
        addNote(notesStmt, "corpusName", corpusMetadata.name)
        addNote(notesStmt, "sourceCollection", corpusMetadata.sourceName.ifNullOrBlank { "!No source name defined!" })
        val url = corpusMetadata.sourceURL?.toString().ifNullOrBlank { "!No source URL defined!" }
        addNote(notesStmt, "sourceCollectionURL", url)
    }

    private fun addNote(notesStmt: Element, attrVal: String, textContent: String) {
        notesStmt.createChild(
            "note", mapOf(
                "type" to attrVal,
                "resp" to "GaLAHaD",
            ), textContent
        )
    }

    /**
     * Add source description to [fileDesc]:
     * <sourceDesc>
     *     <ab>
     *         <idno type="sourceID">[title]</idno>
     *     </ab>
     *     <ab type="date">
     *         <date from="[eraFrom]" to="[eraTo]"/>
     *     </ab>
     * </sourceDesc>
     */
    private fun addSourceDesc(fileDesc: Element) {
        // Only add if not already present (when merging).
        if (fileDesc.childOrNull("sourceDesc") == null) {
            // <sourceDesc>
            val sourceDesc = fileDesc.createChild("sourceDesc")
            // <ab>
            val ab = sourceDesc.createChild("ab")
            // <idno>
            ab.createChild("idno", title, "sourceID")
            // <ab type="date">
            val abDate = sourceDesc.createChild("ab", "", "date")
            // <date>
            abDate.createChild(
                "date", mapOf(
                    "from" to corpusMetadata.eraFrom.toString(),
                    "to" to corpusMetadata.eraTo.toString(),
                )
            )
        }
    }

    /**
     * Add encoding description to [teiHeader]:
     * <encodingDesc>
     *    <appInfo>...</appInfo>
     *    <editorialDecl>...</editorialDecl>
     * </encodingDesc>
     */
    private fun addEncodingDescMetadata(teiHeader: Node) {
        // <encodingDesc>
        val encodingDesc = teiHeader.getOrCreateChild("encodingDesc")
        // <appInfo>
        addAppInfo(encodingDesc)
        // <editorialDecl>
        addEditorialDecl(encodingDesc)
    }

    /**
     * Add app information to [encodingDesc]:
     * <appInfo resp="GaLAHaD">
     *     <application @ident @version @xml:id>
     *         <label>POS-tagger and lemmatiser</label>
     *         <ptr @target>
     *     </application>
     * </appInfo>
     */
    private fun addAppInfo(encodingDesc: Node) {
        // <appInfo>
        val appInfo = encodingDesc.createChild("appInfo", "resp" to "GaLAHaD")
        // <application>
        val application = appInfo.createChild(
            "application", mapOf(
                "version" to export.tagger.version,
                "ident" to export.tagger.id,
                "xml:id" to export.tagger.id,
            )
        )
        // <label>
        application.createChild("label", "POS-tagger and lemmatiser")
        // <ptr>
        application.createChild("ptr", "target" to export.tagger.model.href)
    }

    /**
     * Add editorial declaration to [encodingDesc]:
     * <editorialDecl resp="GaLAHaD">
     *     <interpretation>
     *         <ab>...</ab> // regular
     *         <ab>...</ab> // provenance
     *     </interpretation>
     * </editorialDecl>
     */
    private fun addEditorialDecl(encodingDesc: Element) {
        val editorialDecl = encodingDesc.createChild("editorialDecl", "resp" to "GaLAHaD")
        val interpretation = editorialDecl.createChild("interpretation", "xml:id" to "A0001")
        // Regular <ab>
        val ab = interpretation.createChild(
            "ab", mapOf(
                "type" to "linguisticAnnotation",
                "subtype" to "POS-tagging_lemmatisation",
            )
        )
        addInterGrpTo(
            ab, mapOf(
                "annotationStyle" to "inline",
                "Documentation" to "",
                "annotationSet" to (export.tagger.tagset ?: ""),
                "annotationDescription" to "The file was automatically annotated within the platform GaLAHaD, which is a central hub for enriching historical Dutch.",
                "annotationFormat" to "TEI xml",
            )
        )
        // Provenance <ab>
        addProvenanceAb(interpretation)
    }

    /**
     * Add provenance <ab> to [interpretation]:
     * <ab type="linguisticAnnotation" subtype="POS-tagging_lemmatisationProvenance1">
     *     <interpGrp type="annotationMode">
     *         <interp>automatically annotated</interp>
     *     </interpGrp>
     *     <interpGrp type="processor">
     *         <interp sameAs="#[export.tagger.id]"/>
     *     </interpGrp>
     *     <date from="[date]" to="[date]"/>
     * </ab>
     */
    private fun addProvenanceAb(interpretation: Element) {
        val ab = interpretation.createChild(
            "ab", mapOf(
                "type" to "linguisticAnnotation",
                "subtype" to "POS-tagging_lemmatisationProvenance1",
            )
        )
        addInterGrpTo(ab, "annotationMode", "automatically annotated")
        // processor interp is special, using @sameAs
        val processor = ab.createChild("interpGrp", "type" to "processor")
        processor.createChild("interp", "sameAs" to "#${export.tagger.id}")
        // Provenance also has a <date>
        val nowL: Long = System.currentTimeMillis()
        val now = SimpleDateFormat("yyyy-MM-dd").format(nowL)
        ab.createChild(
            "date", mapOf(
                "from" to now,
                "to" to now,
            )
        )
    }

    /**
     * Add profile description to [teiHeader]:
     * <profileDesc>
     *     <langUsage>
     *         <language ident="nl">
     *             Dutch
     *             <interpGrp type="dominantLanguage">
     *                 <interp>true</interp>
     *             </interpGrp>
     *         </language>
     *     </langUsage>
     * </profileDesc>
     */
    private fun addProfileDescMetadata(teiHeader: Node) {
        val profileDesc = teiHeader.getOrCreateChild("profileDesc")
        // Only add if not already present (when merging).
        if (profileDesc.childOrNull("langUsage") == null) {
            val langUsage = profileDesc.createChild("langUsage")
            val languageName = corpusMetadata.language.ifNullOrBlank { "Dutch" }
            // TODO: note that for @ident we default to dutch
            val language = langUsage.createChild("language", "ident" to "nl", languageName)
            addInterGrpTo(language, "dominantLanguage", "true")
        }
    }

    /**
     * Add interpretation group to [node]:
     * <interpGrp type="[key]">
     *     <interp>[value]</interp>
     * </interpGrp>
     */
    private fun addInterGrpTo(node: Node, key: String, value: String) {
        // <interpGrp type="[key]">
        val interpGrp = node.createChild("interpGrp", "", key)
        // <interp>
        interpGrp.createChild("interp", value)
    }

    private fun addInterGrpTo(node: Node, keyValues: Map<String, String>) {
        keyValues.forEach { (key, value) -> addInterGrpTo(node, key, value) }
    }

}