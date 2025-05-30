package org.ivdnt.galahad.corpora

import org.ivdnt.galahad.documents.Documents
import org.ivdnt.galahad.evaluation.CorpusEvaluations
import org.ivdnt.galahad.files.DiskValue
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import org.ivdnt.galahad.jobs.Jobs
import java.io.File
import java.util.*

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
) : GalahadFolder(dir) {

    val documents: Documents = Documents(dir.resolve(DOCS_FOLDER), this)
    val jobs: Jobs = Jobs(dir.resolve(JOBS_FOLDER), this)
    val evaluation: CorpusEvaluations = CorpusEvaluations(dir.resolve(EVALUATION_FOLDER), this)

    // Files in the corpus folder.
    private val mutableMetadataFile = dir.resolve(MUTABLE_METADATA_FILE)
    private val immutableMetadataFile = dir.resolve(IMMUTABLE_METADATA_FILE)

    val uuid: UUID = UUID.fromString(dir.name)

    /**
     * Convenient access to [MutableCorpusMetadata] without the need to get the expensive [CorpusMetadata]
     * When uploading docs, for example, all we need to know is if the user has permission.
     */
    var mutableMetadata: MutableCorpusMetadata
        get() = DiskValue<MutableCorpusMetadata>(mutableMetadataFile).readOrThrow()
        set(value) {
            DiskValue<MutableCorpusMetadata>(mutableMetadataFile).write(value)
        }

    val immutableMetadata: CorpusMetadata get() = immutableMetadataCache.readOrCreate()

    private val immutableMetadataCache = object : ValidatedDiskValue<CorpusMetadata>(immutableMetadataFile) {
        override fun isValid(lastModified: Long) = lastModified >= this@Corpus.lastModified
        override fun set() = CorpusMetadata.create(this@Corpus)
    }

    companion object {
        private const val MUTABLE_METADATA_FILE = "mutableMetadata.json"
        private const val IMMUTABLE_METADATA_FILE = "immutableMetadata.json"

        private const val JOBS_FOLDER = "jobs"
        private const val DOCS_FOLDER = "documents"
        private const val EVALUATION_FOLDER = "evaluation"

        fun create(dir: File, metadata: MutableCorpusMetadata): Corpus {
            // dummy corpus to access the paths
            val corpus = Corpus(dir)
            // clean, trim, validate, and set owner
            val cleanMetadata = MutableCorpusMetadata.clean(metadata)
            // write metadata to disk
            DiskValue<MutableCorpusMetadata>(corpus.mutableMetadataFile).write(cleanMetadata)
            return corpus
        }
    }
}