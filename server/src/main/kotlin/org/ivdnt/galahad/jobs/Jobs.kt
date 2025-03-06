package org.ivdnt.galahad.jobs

import org.apache.logging.log4j.kotlin.logger
import org.ivdnt.galahad.filesystem.GalahadFile
import org.ivdnt.galahad.app.CRDSet
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.taggers.Tagger

import java.io.File

class Jobs(
    dir: File,
    private val corpus: Corpus,
) : GalahadFile(dir), CRDSet<String, Job, String> {


    // better be verbose than sorry
    fun readAllJobStatesIncludingPotentialJobs(): Set<JobMetadata> {
        val existingJobs = readAll().map { it.metadata }
        val numDocs = corpus.documents.readAll().size
        val potentialJobs = Tagger.taggers.values.map {
            JobMetadata(
                it, Progress(pending = numDocs), LayerPreview.EMPTY, LayerSummary(), 0
            )
        }
        val jobMap = HashMap<String, JobMetadata>()
        potentialJobs.forEach { jobMap[it.tagger.id] = it }
        // Existing jobs take precedence above all, so they are put last.
        existingJobs.forEach { jobMap[it.tagger.id] = it }
        return jobMap.values.toSet()
    }

    override fun readAll(): Set<Job> = dir.list()?.map { readOrThrow(it) }?.toSet() ?: setOf()

    override fun createOrThrow(key: String): Job {
        // accessing the job once creates it and it's directories
        // TODO replace this with job companion object create()
        Job(dir.resolve(key), corpus)
        return readOrThrow(key)
    }

    fun readOrCreateOrThrow(key: String): Job {
        return readOrNull(key) ?: createOrThrow(key)
    }

    override fun readOrNull(key: String): Job? {
        return if (dir.resolve(key).exists()) Job(dir.resolve(key), corpus) else null
    }

    override fun readOrThrow(key: String): Job = readOrNull(key) ?: throw JobNotFoundException(key)

    override fun deleteOrThrow(key: String) {
        readOrThrow(key) // does it exist?
        if (!dir.resolve(key).deleteRecursively()) {
            logger.warn("Partial deletion of $key")
        }
    }
}