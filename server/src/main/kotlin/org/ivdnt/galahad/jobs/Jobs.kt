package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.ivdnt.galahad.filesystem.GalahadFileManager
import org.ivdnt.galahad.taggers.Tagger
import java.io.File

class Jobs(
    dir: File,
    private val corpus: Corpus,
) : GalahadFileManager<Job, String>(dir) {
    override fun createOrThrow(key: String): Job {
        // Throw if the key is not a tagger name (treat source layer as tagger)
        if (key !in Tagger.taggers && key != SOURCE_LAYER_NAME) throw TaggerNotFoundException(key)
        // Safe to create it now
        return ctor(key)
    }

    override fun ctor(key: String) = Job(dir.resolve(key), corpus)
    override fun throwNotFound(key: String) = throw JobNotFoundException(key)

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
}