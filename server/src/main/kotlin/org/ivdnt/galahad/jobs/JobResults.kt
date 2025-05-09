package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.exceptions.DocumentJobNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import java.io.File

/**
 * Create, read and delete document jobs for a job.
 * Represents the "documents/" folder in a job folder.
 * Usage:
 * ```
 * val documentJobs = job.documentJobs // some existing job
 * val key = "..."
 *
 * val docJob = documentJobs.createOrThrow(key)
 * val all = documentJobs.readAll()
 *
 * documentJobs.deleteOrNull(key)
 * if (documentJobs.deleteOrNull(key) == null) { println("Nothing to delete") } // prints
 * // documentJobs.deleteOrThrow(key) // throws
 *
 * val docJob2 = documentJobs.readOrNull(key) // returns null
 * // val docJob3 = documentJobs.readOrThrow(key) // throws
 * ```
 */
class JobResults(
    dir: File,
) : GalahadFolderManager<JobResult, String>(dir) {
    override fun ctor(key: String): JobResult = JobResult(dir.resolve(key))
    override fun throwNotFound(key: String): Nothing = throw DocumentJobNotFoundException(key)
}