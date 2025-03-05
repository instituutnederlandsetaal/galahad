package org.ivdnt.galahad.jobs

import org.apache.logging.log4j.kotlin.logger
import org.ivdnt.galahad.app.CRDSet
import org.ivdnt.galahad.exceptions.DocumentJobNotFoundException
import org.ivdnt.galahad.filesystem.GalahadFile
import java.io.File

class DocumentJobs(
    dir: File,
) : GalahadFile(dir), CRDSet<String, DocumentJob, String> {

    override fun createOrThrow(file: String): DocumentJob {
        return DocumentJob(dir.resolve(file))
    }

    override fun readAll(): Set<DocumentJob> = dir.list()?.map { readOrThrow(it) }?.toSet() ?: setOf()

    override fun readOrNull(key: String) =
        if (dir.resolve(key).exists()) DocumentJob(dir.resolve(key)) else null

    override fun readOrThrow(key: String) = readOrNull(key) ?: throw DocumentJobNotFoundException(key)

    override fun deleteOrThrow(key: String) {
        readOrThrow(key) // does it exist?
        if (!dir.resolve(key).deleteRecursively()) {
            logger.warn("Partial deletion of $key")
        }
    }
}