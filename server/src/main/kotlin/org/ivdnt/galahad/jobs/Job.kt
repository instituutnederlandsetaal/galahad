package org.ivdnt.galahad.jobs

import java.io.File
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import org.ivdnt.galahad.layers.CorpusLayerMetadata

/**
 * A job is saved to disk as a folder under jobs/ (managed by [Jobs]), with the following files:
 *
 * - documents/: a folder containing all documents in the job. A single document is represented by
 *   [JobResult]. These can be retrieved with [readOrThrow].
 * - _isActive: a file that stores whether the job is currently being processed by the tagger.
 */
class Job(
    dir: File, // the name of this directory is the name of the job/tagger
    val corpus: Corpus,
) : GalahadFolder(dir), Logging {

    val results: JobResults = JobResults(dir.resolve(DOCUMENT_JOBS_FOLDER))

    // Files
    private val isActiveFile: File = dir.resolve(IS_ACTIVE_FILE)
    private val metadataFile: File = dir.resolve(METADATA_FILE)

    // Values
    val hasResult: Boolean
        get() =
            name != SOURCE_LAYER &&
                results.readAllSequence().any { it.status == JobStatus.FINISHED }

    /** Progress of the job based on the status of the [JobResult]s of this job. */
    val progress: Progress
        get() {
            // NOTE: The number of documents is not the same as the number of document jobs.
            // Example: after running a job, a user has added more documents to the corpus.
            // So for calculating progress, we need to look at the number of corpus documents.
            val docs = corpus.documents.readAll()
            // If a document is not in the list of documentJobs, it is pending by default.
            val statuses = docs.map { results.readOrNull(it.name)?.status ?: JobStatus.PENDING }
            // For errors however, we can just look at the documentJobs.
            val errors =
                results.readAll().mapNotNull { it.error?.let { error -> name to error } }.toMap()
            return Progress(
                pending = statuses.count { it == JobStatus.PENDING },
                processing = (if (JobController.inQueue(this)) 1 else 0),
                failed = statuses.count { it == JobStatus.ERROR },
                finished = statuses.count { it == JobStatus.FINISHED },
                errors = errors,
            )
        }

    /**
     * Whether the job is currently being processed (i.e. has sent files to the tagger to become
     * tagged at some point).
     */
    var isActive: Boolean
        get() = isActiveFile.exists()
        set(value) {
            if (value) {
                isActiveFile.createNewFile()
            } else {
                isActiveFile.delete()
            }
        }

    val metadata: CorpusLayerMetadata
        get() = metadataCache.readOrCreate()

    /**
     * The state of the job, which is cached in a file. This is a very expensive operation, so we
     * want to cache it.
     */
    private val metadataCache =
        object : ValidatedDiskValue<CorpusLayerMetadata>(metadataFile) {
            // NOTE: we also check against the last modified of the documents folder: adding new
            // docs
            // should invalidate the cache.
            override fun isValid(modified: Long) =
                modified >= this@Job.modified && modified >= corpus.documents.modified

            override fun set() = CorpusLayerMetadata.create(this@Job)
        }

    // fun getLayer(doc: Document): Layer = getLayer(doc.name)

    // fun getLayer(key: String): Layer = results.readOrNull(key)?.layer ?: Layer.EMPTY

    //    fun setLayer(key: String, layer: Layer) {
    //        results.createOrThrow(key).layer = layer
    //    }

    // fun setLayer(doc: Document, layer: Layer): Unit = setLayer(doc.name, layer)

    fun start() {
        JobController.queue(this)
    }

    fun stop() {
        JobController.dequeue(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Job) return false
        // Two jobs are equal if they have the same name and corpus name
        return this.corpus.name == other.corpus.name && this.name == other.name
    }

    companion object {
        private const val DOCUMENT_JOBS_FOLDER = "documents"

        /** Number of documents at the tagger per job */
        private const val IS_ACTIVE_FILE = "active"
        private const val METADATA_FILE = "metadata.json"
    }
}
