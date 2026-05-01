package org.ivdnt.galahad.documents

import java.io.File
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER_NAME
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.files.DiskValue
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.util.ThreadPoolUtil

/**
 * Documents are saved as folders with their file name as folder name, including extension.
 * Initializing a document object resolves a path, but does not fill the folder with data. Use
 * [Document.create] instead.
 *
 * A folder has the following files, that store the document's data:
 * - metadata.json: a cache file storing [DocumentMetadata] about the document
 * - source/[name]: the source file
 */
class Document(dir: File) : GalahadFolder(dir), Logging {
    val sourceFile: File = dir.resolve("source").resolve(name)
    val metadata: DocumentMetadata by lazy {
        try { // For sake of backwards compatibility with GaLAHaD 1.x.x, we create metadata if not
            // present.
            DiskValue<DocumentMetadata>(dir.resolve(METADATA_FILE)).readOrThrow()
        } catch (e: Exception) {
            logger.warn("Error reading metadata file, creating new metadata", e)
            val metadata = DocumentMetadata.create(ParsedFile.create(sourceFile))
            DiskValue<DocumentMetadata>(dir.resolve(METADATA_FILE)).write(metadata)
        }
    }

    internal companion object {
        private const val METADATA_FILE = "metadata.json"

        /**
         * Create a new document folder from an uploaded file and fill it with the necessary data.
         */
        fun create(dir: File, file: File, corpus: Corpus): Document {
            val parsedFile = ParsedFile.create(file)
            // First try to access the layer. If the file is invalid, this will throw.
            val sourceLayer = parsedFile.layer
            // Create a document to access the paths
            val doc = Document(dir)
            // Set sourceLayer as job. Note that if we threw, we don't unnecessarily create a job
            // folder,
            // keeping the disk clean.
            corpus.jobs.createOrThrow(SOURCE_LAYER_NAME).setLayer(doc.name, sourceLayer)
            // metadata; needs to be serialized as well
            DiskValue<DocumentMetadata>(dir.resolve(METADATA_FILE))
                .write(DocumentMetadata.create(parsedFile))
            // move uploaded file in the background
            ThreadPoolUtil.pool.execute { file.copyTo(doc.sourceFile, overwrite = true) }
            // The same document object is now valid: it's folder data has been filled.
            return doc
        }
    }
}
