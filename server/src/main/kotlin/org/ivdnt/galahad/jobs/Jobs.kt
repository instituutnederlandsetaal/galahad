package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.app.CRDSet
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.taggers.TaggerStore
import java.io.File

class Jobs(
    workDirectory: File,
    private val corpus: Corpus,
) : BaseFileSystemStore(workDirectory), CRDSet<String, Job, String> {

    private val taggerStore = TaggerStore()

    fun readAllExistingJobs(): Set<JobState> = readAll().map { it.state }.toSet()

    // better be verbose than sorry
    fun readAllJobStatesIncludingPotentialJobs(): Set<JobState> {
        val existingJobs = readAll().map { it.state }
        val potentialJobs = taggerStore.taggers.map { it.expensiveGet() }.map {
            JobState(
                it, Progress(pending = corpus.documents.readAll().size), LayerPreview.EMPTY, LayerSummary(), 0
            )
        }
        val sourceJobs = setOf(
            JobState(
                tagger = corpus.sourceTagger.expensiveGet()
            )
        )
        // the latter overrides the former’s value
        val jobMap = HashMap<String, JobState>()
        potentialJobs.forEach { jobMap[it.tagger.id] = it }
        sourceJobs.forEach { jobMap[it.tagger.id] = it }
        // Existing jobs take precedence above all, so they are put last.
        existingJobs.forEach { jobMap[it.tagger.id] = it }
        return jobMap.values.toSet()
    }

    override fun readAll(): Set<Job> =
        workDirectory.list()?.map { readOrThrow(it) }?.toSet() ?: throw Exception("Could not list jobs")

    override fun createOrNull(key: String): Job? {
        // accessing the job once creates it and it's directories
        Job(workDirectory.resolve(key), corpus)
        return readOrNull(key)
    }

    override fun readOrNull(key: String): Job? {
        if (key.isBlank()) throw Exception("Blank job name not allowed") // An empty job name can not be resolved
        return if (workDirectory.resolve(key).exists()) Job(workDirectory.resolve(key), corpus) else null
    }

    override fun readOrThrow(key: String): Job {
        // A job name corresponds with a tagger name.
        // For sake of clarity, i.e. being able to throw a more specific exception
        // We first check if the tagger exists
        if (key != SOURCE_LAYER_NAME) {
            taggerStore.getSummaryOrThrow(key).expensiveGet() // throws TaggerNotFoundException
        }
        return readOrNull(key) ?: throw JobNotFoundException(key)
    }

    override fun delete(key: String) {
        workDirectory.resolve(key).deleteRecursively()
    }
}