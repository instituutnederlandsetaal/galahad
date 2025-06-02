package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.entities.JobsEntities
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import org.ivdnt.galahad.files.ValidatedDiskValue
import java.io.File

class JobsEvaluations(
    dir: File,
    private val corpus: Corpus,
) : GalahadFolderManager<JobEvaluation, JobPair>(dir) {
    fun ctor(key: JobPair): JobEvaluation = JobEvaluation(dir.resolve(key.toString()), corpus, key)
    override fun ctor(key: String): JobEvaluation = ctor(JobPair.fromString(key))
    override fun throwNotFound(key: String): Nothing = throw JobNotFoundException(key)

    val entities: JobsEntities get() = entitiesCache.readOrCreate()
    private val entitiesFile = dir.resolve(ENTITIES_FILE)
    private val entitiesCache = object : ValidatedDiskValue<JobsEntities>(entitiesFile) {
        override fun isValid(lastModified: Long) = lastModified >= corpus.lastModified
        override fun set(): JobsEntities = JobsEntities.create(corpus, this@JobsEvaluations)
    }

    companion object {
        private const val ENTITIES_FILE = "entities.json"
    }
}

