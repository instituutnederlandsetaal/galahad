package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.entities.CorpusEntities
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import org.ivdnt.galahad.files.ValidatedDiskValue
import java.io.File

/** Defines evaluations at the level of a corpus, i.e. where all jobs (and their documents) are accumulated in some manner. */
class CorpusEvaluation(
    dir: File,
    private val corpus: Corpus,
) : GalahadFolderManager<JobEvaluation, JobPair>(dir) {
    fun ctor(key: JobPair): JobEvaluation = JobEvaluation(dir.resolve(key.toString()), corpus, key)
    override fun ctor(key: String): JobEvaluation = ctor(JobPair.fromString(key))
    override fun throwNotFound(key: String): Nothing = throw JobNotFoundException(key)

    val entities: CorpusEntities get() = object : ValidatedDiskValue<CorpusEntities>(dir.resolve(ENTITIES_FILE)) {
        override fun isValid(modified: Long) = modified >= corpus.modified
        override fun set(): CorpusEntities = CorpusEntities.create(corpus, this@CorpusEvaluation)
    }.readOrCreate()

    companion object {
        private const val ENTITIES_FILE = "entities.json"
    }
}

