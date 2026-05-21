package org.ivdnt.galahad.jobs

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import java.io.File

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

    val metadata: JobMetadata
        get() =
            object : ValidatedDiskValue<JobMetadata>(dir.resolve(METADATA_FILE)) {
                    override fun isValid(modified: Long) = modified >= this@Job.modified

                    override fun set(): JobMetadata = JobMetadata.create(this@Job, corpus)
                }
                .readOrCreate()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Job) return false
        // Two jobs are equal if they have the same name and corpus name
        return this.corpus.name == other.corpus.name && this.name == other.name
    }

    companion object {
        private const val DOCUMENT_JOBS_FOLDER = "documents"
        private const val METADATA_FILE = "metadata.json"
    }
}
