package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import java.io.File

class CorpusEvaluations(
    dir: File,
    private val corpus: Corpus,
) : GalahadFolderManager<JobEvaluations, JobPair>(dir) {
    fun ctor(key: JobPair): JobEvaluations = JobEvaluations(dir.resolve(key.toString()), corpus, key)
    override fun ctor(key: String): JobEvaluations = ctor(JobPair.fromString(key))
    override fun throwNotFound(key: String): Nothing = throw JobNotFoundException(key)

}

