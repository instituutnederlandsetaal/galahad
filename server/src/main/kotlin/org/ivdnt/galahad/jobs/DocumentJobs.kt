package org.ivdnt.galahad.jobs

import org.apache.logging.log4j.kotlin.logger
import org.ivdnt.galahad.app.CRDSet
import org.ivdnt.galahad.exceptions.DocumentJobNotFoundException
import org.ivdnt.galahad.filesystem.GalahadFile
import org.ivdnt.galahad.filesystem.GalahadFileManager
import java.io.File

class DocumentJobs(
    dir: File,
) : GalahadFileManager<DocumentJob, String>(dir) {
    override fun createOrThrow(key: String) = ctor(key)
    override fun ctor(key: String) = DocumentJob(dir.resolve(key))
    override fun throwNotFound(key: String) = throw DocumentJobNotFoundException(key)
}