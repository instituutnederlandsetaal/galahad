package org.ivdnt.galahad.data.document

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.FileBackedValue
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.formats.conllu.export.LayerToConlluConverter
import org.ivdnt.galahad.formats.folia.export.LayerToFoliaConverter
import org.ivdnt.galahad.formats.naf.export.LayerToNAFConverter
import org.ivdnt.galahad.formats.tei.export.LayerToTEIConverter
import org.ivdnt.galahad.formats.tsv.export.LayerToTSVConverter
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.io.path.createTempDirectory

const val SOURCE_LAYER_NAME = "sourceLayer"
const val PREVIEW_LENGTH: Int = 100

/**
 * Documents are saved as folders with their file name as folder name, including extension.
 * Initializing a document object resolves a path, but does not yet [Document.parse] the document.
 * Parsing is done on demand, as it is an expensive operation.
 *
 * A folder can have the following files, that store the document's data:
 * - format: the [DocumentFormat] of the document.
 * - plaintext: the document's text content
 * - uuid: a unique identifier for the document. Used as a metadata pid when converting a layer to TEI.
 * - sourceLayer: the document's source annotations as a [Layer]
 * - metadata.cache: a cache file storing [DocumentMetadata] about the document
 * - uploaded/[name]: the uploaded raw file
 */
class Document(
    dir: File,
) : BaseFileSystemStore(
    dir
), Logging {
    /** File name including extension */
    val name: String = dir.name

    // Files in the document folder.
    private val formatFile = dir.resolve("format")
    val plainTextFile = dir.resolve("plaintext")
    private val uuidFile = dir.resolve("uuid")
    private val metadataFile = dir.resolve("metadata")
    private val sourceLayerFile = dir.resolve(SOURCE_LAYER_NAME)
    val uploadedFile = dir.resolve("uploaded").resolve(name)

    // Values in those files.

    val format: DocumentFormat
        get() = DocumentFormat.fromString(formatFile.readText())

    val plaintext: String
        get() = plainTextFile.readText()

    /**
     * The UUID is only used as a metadata pid when converting a layer to TEI (for now).
     * When merging TEI, it is only used if the document itself defines no pid.
     */
    val uuid: UUID
        get() = UUID.fromString(uuidFile.readText())

    /**
     * For the sake of backwards compatibility with GaLAHaD 1.x.x,
     * we will create the metadata if is not present.
     */
    val metadata: DocumentMetadata
        get() {
            if (!metadataFile.exists()) {
                logger.debug("Document Metadata file not found, creating new metadata")
                val metadata = createDocMeta(name, format, plaintext, sourceLayer)
                FileBackedValue(metadataFile, DocumentMetadata.EMPTY).modify<DocumentMetadata> { metadata }
                return metadata
            }
            return FileBackedValue(metadataFile, DocumentMetadata.EMPTY).read<DocumentMetadata>()
        }

    /** Source annotations, if present. Saved to disk. */
    val sourceLayer: Layer
        get() = FileBackedValue(sourceLayerFile, Layer.EMPTY).read<Layer>()

    val internalFile: InternalFile
        get() = InternalFile.create(uploadedFile, format).expensiveGet()

    /** Convert document to desired format. */
    fun convert(transformMetadata: DocumentTransformMetadata): File {
        val docName = workDirectory.resolve(transformMetadata.document.name).nameWithoutExtension
        return when (transformMetadata.targetFormat) {
            // The file is what we are interested in, and it is expensive to initialize the documents, so we just pass the file
            DocumentFormat.Folia -> LayerToFoliaConverter(transformMetadata).convertToFileNamed(docName)
            DocumentFormat.Naf -> LayerToNAFConverter(transformMetadata).convertToFileNamed(docName)
            DocumentFormat.TeiP5 -> LayerToTEIConverter(transformMetadata).convertToFileNamed(docName)
            DocumentFormat.Tsv -> LayerToTSVConverter(transformMetadata).convertToFileNamed(docName)
            DocumentFormat.Conllu -> LayerToConlluConverter(transformMetadata).convertToFileNamed(docName)
            DocumentFormat.Txt -> {
                val tempPath = createTempDirectory("galahad-layer-converter")
                Files.copy(
                    plainTextFile.toPath(), Paths.get("$tempPath/$docName.txt"), StandardCopyOption.REPLACE_EXISTING
                )
                File(tempPath.toString(), "$docName.txt")
            }

            else -> throw Exception("Conversion to ${transformMetadata.targetFormat} not supported")
        }
    }

    /** Merge an annotation layer with the original uploaded file, retaining the document structure. */
    fun merge(transformMetadata: DocumentTransformMetadata) =
        internalFile.merge(transformMetadata)

    internal companion object {
        /**
         * Create a new document folder from an uploaded file and fill it with the necessary data.
         */
        fun create(dir: File, file: File): Document {
            // Create a document to access the paths
            val doc = Document(dir)

            // uploaded file
            file.copyTo(doc.uploadedFile)

            // format
            val format = FormatInducer.determineFormat(file)
            doc.formatFile.writeText(format.identifier)

            // uuid
            val randomUuid = UUID.randomUUID()
            doc.uuidFile.writeText(randomUuid.toString())

            // plaintext & sourceLayer
            val internalFile = InternalFile.create(file, format).expensiveGet()
            val plainText = internalFile.plainText()
            val sourceLayer = internalFile.sourceLayer()
            // plaintext
            doc.plainTextFile.writeText(plainText)
            // sourceLayer
            // needs to be serialized, so writeText is not sufficient.
            FileBackedValue(doc.sourceLayerFile, Layer.EMPTY).modify<Layer> { sourceLayer }

            // metadata
            val metadata = createDocMeta(file.name, format, plainText, sourceLayer)
            // needs to be serialized as well
            FileBackedValue(doc.metadataFile, DocumentMetadata.EMPTY).modify<DocumentMetadata> { metadata }

            // The same document object is now valid: it's folder data has been filled.
            return doc
        }

        private fun createDocMeta(
            fileName: String,
            format: DocumentFormat,
            plainText: String,
            sourceLayer: Layer,
        ): DocumentMetadata {
            val metadata = DocumentMetadata(
                name = fileName,
                format = format.identifier,
                numChars = plainText.length,
                numAlphabeticChars = plainText.filter { it.isLetter() }.length,
                preview = plainText.take(PREVIEW_LENGTH) + if (plainText.length > PREVIEW_LENGTH) "..." else "",
                layerSummary = sourceLayer.summary,
                lastModified = System.currentTimeMillis()
            )
            return metadata
        }
    }
}

