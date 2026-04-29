package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.DocumentNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import java.io.File

class DocumentEvaluations(
    dir: File,
    private val corpus: Corpus,
    val jobs: JobPair,
) : GalahadFolderManager<DocumentEvaluation, String>(dir) {
    override fun ctor(key: String): DocumentEvaluation = DocumentEvaluation(dir.resolve(key), corpus, jobs)
    override fun throwNotFound(key: String): Nothing = throw DocumentNotFoundException(key)
}