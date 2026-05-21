package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import org.ivdnt.galahad.taggers.Tagger
import java.io.File

/**
 * Create, read and delete jobs in a corpus. Represents the "jobs/" folder in a corpus folder.
 * Usage:
 * ```
 * val jobs = corpus.jobs // some existing corpus
 * val key = "..."
 *
 * val job = jobs.createOrThrow(key)
 * val all = jobs.readAll()
 *
 * jobs.deleteOrNull(key)
 * if (jobs.deleteOrNull(key) == null) { println("Nothing to delete") } // prints
 * // jobs.deleteOrThrow(key) // throws
 *
 * val job2 = jobs.readOrNull(key) // returns null
 * // val job3 = jobs.readOrThrow(key) // throws
 * ```
 */
class Jobs(dir: File, private val corpus: Corpus) : GalahadFolderManager<Job, String>(dir) {
    override fun createOrThrow(key: String): Job {
        // Throw if the key is not a tagger
        if (key !in Tagger.taggers) throw TaggerNotFoundException(key)
        // Safe to create it now
        return ctor(key).also { JobController.queue(it) }
    }

    override fun ctor(key: String): Job = Job(dir.resolve(key), corpus)

    override fun throwNotFound(key: String): Nothing = throw JobNotFoundException(key)

    override fun deleteOrThrow(key: String) {
        JobController.dequeue(readOrThrow(key))
        super.deleteOrThrow(key)
    }
}
