package org.ivdnt.galahad.data.document

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.filesystem.FileBackedValue
import org.ivdnt.galahad.filesystem.GalahadFile
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
import kotlin.io.path.createTempDirectory

const val SOURCE_LAYER_NAME = "sourceLayer"
const val METADATA_FILE = "metadata.json"
const val PLAINTEXT_FILE = "plaintext.txt"

/**
 * Documents are saved as folders with their file name as folder name, including extension.
 * Initializing a document object resolves a path, but does not fill the folder with data.
 * Use [Document.create] instead.
 *
 * A folder has the following files, that store the document's data:
 * - plaintext.txt: the document's text content
 * - sourceLayer.json: the document's source annotations as a [Layer]
 * - metadata.json: a cache file storing [DocumentMetadata] about the document
 * - uploaded/[name]: the uploaded raw file
 */
class Document(
    dir: File,
) : GalahadFile(dir), Logging {
    // Files in the document folder.
    val plainTextFile = dir.resolve(PLAINTEXT_FILE)
    private val metadataFile = dir.resolve(METADATA_FILE)
    val uploadedFile = dir.resolve("uploaded").resolve(name)

    // Values in those files.

    /** The plaintext content of the document. */
    val plaintext: String by lazy {
        try {
            plainTextFile.readText()
        } catch (e: Exception) {
            logger.error("Error reading plaintext file, creating new plaintext", e)
            val internalFile = InternalFile.create(uploadedFile)
            val text = internalFile.plaintext
            plainTextFile.writeText(text)
            text
        }
    }

    /**
     * The UUID is only used as a metadata pid when converting a layer to TEI (for now).
     * When merging TEI, it is only used if the document itself defines no pid.
     */
    val metadata: DocumentMetadata by lazy {
        // For the sake of backwards compatibility with GaLAHaD 1.x.x, we create metadata if not present.
        try {
            FileBackedValue<DocumentMetadata>(metadataFile).readOrThrow()
        } catch (e: Exception) {
            logger.error("Error reading metadata file, creating new metadata", e)
            val metadata = DocumentMetadata.create(internalFile)
            FileBackedValue<DocumentMetadata>(metadataFile).write(metadata)
        }
    }

    /** [DocumentFormat]-parsed file. */
    val internalFile: InternalFile by lazy { InternalFile.create(uploadedFile) }

    /** Convert document to desired format. */
    fun convert(transformMetadata: DocumentTransformMetadata): File {
        val docName = uploadedFile.nameWithoutExtension
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
    fun merge(transformMetadata: DocumentTransformMetadata) = internalFile.merge(transformMetadata)

    internal companion object {
        /**
         * Create a new document folder from an uploaded file and fill it with the necessary data.
         */
        fun create(dir: File, file: File, corpus: Corpus): Document {
            // Create a document to access the paths
            val doc = Document(dir)

            // uploaded file
            file.copyTo(doc.uploadedFile)

            // plaintext & sourceLayer
            val internalFile = InternalFile.create(file)
            // sourceLayer as job
            corpus.jobs.createOrThrow(SOURCE_LAYER_NAME).documentJobs.createOrThrow(doc.name).layer =
                internalFile.sourceLayer
            // plaintext
            doc.plainTextFile.writeText(internalFile.plaintext)

            // metadata; needs to be serialized as well
            FileBackedValue<DocumentMetadata>(doc.metadataFile).write(DocumentMetadata.create(internalFile))

            // The same document object is now valid: it's folder data has been filled.
            return doc
        }
    }
}

