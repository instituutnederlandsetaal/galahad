package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.annotations.LayerPreview
import org.ivdnt.galahad.annotations.LayerAnnotations
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import org.ivdnt.galahad.taggers.Tagger
import java.io.File

/**
 * Create, read and delete jobs in a corpus.
 * Represents the "jobs/" folder in a corpus folder.
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
class Jobs(
    dir: File,
    private val corpus: Corpus,
) : GalahadFolderManager<Job, String>(dir) {
    override fun createOrThrow(key: String): Job {
        // Throw if the key is not a tagger name (treat source layer as tagger)
        if (key !in Tagger.taggers && key != SOURCE_LAYER_NAME) throw TaggerNotFoundException(key)
        // Safe to create it now
        return ctor(key)
    }

    override fun ctor(key: String): Job = Job(dir.resolve(key), corpus)
    override fun throwNotFound(key: String): Nothing = throw JobNotFoundException(key)
    override fun deleteOrThrow(key: String) {
        JobController.dequeue(readOrThrow(key))
        super.deleteOrThrow(key)
    }

    fun readAllMetadata(): List<JobMetadata> {
        // Create a map of all taggers with empty metadata
        val numDocs = corpus.immutableMetadata.numDocs
        val allJobs = Tagger.taggers.mapValues {
            JobMetadata(it.value, Progress(numDocs), LayerPreview.EMPTY, LayerAnnotations.EMPTY, 0)
        }
        // replace the entries for which a job exists
        val jobs = readAll().map { it.metadata }.associateBy { it.tagger.id }
        // Replacement is simply plus
        return (allJobs + jobs).values.toList()
    }
}