package org.ivdnt.galahad.jobs

import org.apache.logging.log4j.kotlin.logger
import org.ivdnt.galahad.filesystem.GalahadFile
import org.ivdnt.galahad.app.CRDSet
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.taggers.TaggerStore
import java.io.File

class Jobs(
    dir: File,
    private val corpus: Corpus,
) : GalahadFile(dir), CRDSet<String, Job, String> {

    private val taggerStore = TaggerStore()

    fun readAllExistingJobs(): Set<JobMetadata> = readAll().map { it.metadata }.toSet()

    // better be verbose than sorry
    fun readAllJobStatesIncludingPotentialJobs(): Set<JobMetadata> {
        val existingJobs = readAll().map { it.metadata }
        val potentialJobs = taggerStore.taggers.map { it.expensiveGet() }.map {
            JobMetadata(
                it, Progress(pending = corpus.documents.readAll().size), LayerPreview.EMPTY, LayerSummary(), 0
            )
        }
        val sourceJobs = setOf(
            JobMetadata(
                tagger = corpus.sourceTagger.expensiveGet()
            )
        )
        // the latter overrides the former’s value
        val jobMap = HashMap<String, JobMetadata>()
        potentialJobs.forEach { jobMap[it.tagger.id] = it }
        sourceJobs.forEach { jobMap[it.tagger.id] = it }
        // Existing jobs take precedence above all, so they are put last.
        existingJobs.forEach { jobMap[it.tagger.id] = it }
        return jobMap.values.toSet()
    }

    override fun readAll(): Set<Job> =
        dir.list()?.map { readOrThrow(it) }?.toSet() ?: setOf()

    override fun createOrThrow(key: String): Job {
        // accessing the job once creates it and it's directories
        // TODO replace this with job companion object create()
        Job(dir.resolve(key), corpus)
        return readOrThrow(key)
    }

    override fun readOrNull(key: String): Job? {
        // job name is not a tagger name or the source layer
        if (!taggerStore.ids.contains(name) && key != SOURCE_LAYER_NAME) {
            return null
        }
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