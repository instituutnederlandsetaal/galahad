package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.exceptions.DocumentJobNotFoundException
import org.ivdnt.galahad.filesystem.GalahadFolderManager
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
class DocumentJobs(
    dir: File,
) : GalahadFolderManager<DocumentJob, String>(dir) {
    override fun createOrThrow(key: String) = ctor(key)
    override fun ctor(key: String) = DocumentJob(dir.resolve(key))
    override fun throwNotFound(key: String) = throw DocumentJobNotFoundException(key)
}