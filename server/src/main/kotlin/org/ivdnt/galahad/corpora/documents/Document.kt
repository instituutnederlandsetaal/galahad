package org.ivdnt.galahad.corpora.documents

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.files.DiskValue
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.formats.InternalFile
import java.io.File

/**
 * Documents are saved as folders with their file name as folder name, including extension.
 * Initializing a document object resolves a path, but does not fill the folder with data.
 * Use [Document.create] instead.
 *
 * A folder has the following files, that store the document's data:
 * - plaintext.txt: the document's text content
 * - metadata.json: a cache file storing [DocumentMetadata] about the document
 * - uploaded/[name]: the uploaded raw file
 */
class Document(
    dir: File,
) : GalahadFolder(dir), Logging {
    // Files in the document folder.
    val plaintextFile: File = dir.resolve(PLAINTEXT_FILE)
    private val metadataFile = dir.resolve(METADATA_FILE)
    val uploadedFile: File = dir.resolve("uploaded").resolve(name)

    // Values in those files.

    /** The plaintext content of the document. */
    val plaintext: String by lazy {
        try {
            plaintextFile.readText()
        } catch (e: Exception) {
            logger.error("Error reading plaintext file, creating new plaintext", e)
            val internalFile = InternalFile.create(uploadedFile)
            val text = internalFile.plaintext
            plaintextFile.writeText(text)
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
            DiskValue<DocumentMetadata>(metadataFile).readOrThrow()
        } catch (e: Exception) {
            logger.error("Error reading metadata file, creating new metadata", e)
            val metadata = DocumentMetadata.create(internalFile)
            DiskValue<DocumentMetadata>(metadataFile).write(metadata)
        }
    }

    /** [DocumentFormat]-parsed file. */
    private val internalFile: InternalFile by lazy { InternalFile.create(uploadedFile) }

    internal companion object {
        private const val METADATA_FILE = "metadata.json"
        private const val PLAINTEXT_FILE = "plaintext.txt"

        /**
         * Create a new document folder from an uploaded file and fill it with the necessary data.
         */
        fun create(dir: File, file: File, corpus: Corpus): Document {
            // Create a document to access the paths
            val doc = Document(dir)

            // uploaded file
            file.copyTo(doc.uploadedFile, overwrite = true)

            // plaintext & sourceLayer
            val internalFile = InternalFile.create(file)
            // sourceLayer as job
            corpus.jobs.createOrThrow(SOURCE_LAYER_NAME).jobDocuments.createOrThrow(doc.name).layer =
                internalFile.layer
            // plaintext
            doc.plaintextFile.writeText(internalFile.plaintext)

            // metadata; needs to be serialized as well
            DiskValue<DocumentMetadata>(doc.metadataFile).write(DocumentMetadata.create(internalFile))

            // The same document object is now valid: it's folder data has been filled.
            return doc
        }
    }
}

