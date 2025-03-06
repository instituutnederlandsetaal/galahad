package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.exceptions.DocumentJobNotFoundException
import org.ivdnt.galahad.filesystem.GalahadFileManager
import java.io.File

class DocumentJobs(
    dir: File,
) : GalahadFileManager<DocumentJob, String>(dir) {
    override fun createOrThrow(key: String) = ctor(key)
    override fun ctor(key: String) = DocumentJob(dir.resolve(key))
    override fun throwNotFound(key: String) = throw DocumentJobNotFoundException(key)
}