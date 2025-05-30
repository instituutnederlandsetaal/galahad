package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import java.io.File

class JobsEvaluations(
    dir: File,
    private val corpus: Corpus,
) : GalahadFolderManager<JobEvaluation, JobPair>(dir) {
    fun ctor(key: JobPair): JobEvaluation = JobEvaluation(dir.resolve(key.toString()), corpus, key)
    override fun ctor(key: String): JobEvaluation = ctor(JobPair.fromString(key))
    override fun throwNotFound(key: String): Nothing = throw JobNotFoundException(key)
}

