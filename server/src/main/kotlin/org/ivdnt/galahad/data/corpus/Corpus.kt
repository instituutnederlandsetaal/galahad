package org.ivdnt.galahad.data.corpus

import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.document.Documents
import org.ivdnt.galahad.filesystem.FileBackedCache
import org.ivdnt.galahad.filesystem.FileBackedValue
import org.ivdnt.galahad.filesystem.GalahadFile
import org.ivdnt.galahad.formats.CmdiMetadata
import org.ivdnt.galahad.formats.CorpusTransformMetadata
import org.ivdnt.galahad.jobs.Jobs
import org.ivdnt.galahad.util.createZipFile
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.util.*
import kotlin.io.path.createTempDirectory

private const val MUTABLE_METADATA_FILE = "mutableMetadata.json"
private const val IMMUTABLE_METADATA_FILE = "immutableMetadata.json"

private const val JOBS_FOLDER = "jobs"
private const val DOCS_FOLDER = "documents"

/**
 * A corpus is a collection of documents, metadata and jobs, saved to a folder. The folder contents are:
 *
 * - documents/: a folder containing all documents in the corpus. Represented by [Documents].
 * - jobs/: a folder containing all jobs that were active at some point in the corpus. The sourceLayer is one of them. Represented by [Jobs].
 * - metadata: a cache file storing [MutableCorpusMetadata] about the corpus.
 * - metadata.cache: a cache file storing [CorpusMetadata] about the corpus.
 *
 * A Corpus has an owner, who can add collaborators and viewers.
 * Collaborators have read and write access.
 * Viewers have read access.
 * Admins have access to all corpora with read and write access.
 */
class Corpus(
    dir: File,
) : GalahadFile(dir) {

    val documents = Documents(dir.resolve(DOCS_FOLDER), this)
    val jobs = Jobs(dir.resolve(JOBS_FOLDER), this)

    // Files in the corpus folder.
    private val mutableMetadataFile = dir.resolve(MUTABLE_METADATA_FILE)
    private val immutableMetadataFile = dir.resolve(IMMUTABLE_METADATA_FILE)

    val uuid: UUID = UUID.fromString(dir.name)

    /**
     * Convenient access to [MutableCorpusMetadata] without the need to get the expensive [CorpusMetadata]
     * When uploading docs, for example, all we need to know is if the user has permission.
     */
    var mutableMetadata: MutableCorpusMetadata
        get() = FileBackedValue<MutableCorpusMetadata>(mutableMetadataFile).readOrThrow()
        set(value) {
            FileBackedValue<MutableCorpusMetadata>(mutableMetadataFile).write(value)
        }

    val immutableMetadata: CorpusMetadata
        get() = immutableMetadataCache.read()

    private val immutableMetadataCache = object : FileBackedCache<CorpusMetadata>(immutableMetadataFile) {
        override fun isValid(lastModified: Long) = lastModified >= this@Corpus.lastModified
        override fun set() = CorpusMetadata.create(this@Corpus)
    }

    /**
     * Maps all [Document] found in [Documents] to the desired [DocumentFormat] and zips them. [formatMapper] should perform the mapping.
     */
    fun export(
        ctm: CorpusTransformMetadata,
        formatMapper: (Document) -> File,
        filter: (Document) -> Boolean,
        outputStream: OutputStream? = null,
    ): File {
        val documents = documents.readAll().filter(filter)
        val convertedDocs = documents.asSequence().map(formatMapper)
        val docsToCmdi = documents.asSequence().map { CmdiMetadata(ctm.documentMetadata(it.name)).file }
        val cmdiZip = createZipFile(docsToCmdi, includeCMDI = true)
        // rename the cmdiZip to "metadata"
        val dest = File(createTempDirectory("metadata").toFile(), "metadata.zip")
        Files.move(cmdiZip.toPath(), dest.toPath())
        return createZipFile(convertedDocs + dest, outputStream)
    }

    companion object {
        fun create(user: User, dir: File, metadata: MutableCorpusMetadata): Corpus {
            // dummy corpus to access the paths
            val corpus = Corpus(dir)
            // clean, trim, validate, and set owner
            val cleanMetadata = MutableCorpusMetadata.clean(user, metadata)
            // write metadata to disk
            FileBackedValue<MutableCorpusMetadata>(corpus.mutableMetadataFile).write(cleanMetadata)
            return corpus
        }
    }
}