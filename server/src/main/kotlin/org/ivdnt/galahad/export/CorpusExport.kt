package org.ivdnt.galahad.export

import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.corpora.jobs.Job

open class CorpusExport private constructor(
    val corpus: Corpus,
    val job: Job,
    val user: User,
    val targetFormat: DocumentFormat,
    val posHeadOnly: Boolean,
) {
    companion object {
        fun create(
            corpus: Corpus, jobName: String, format: DocumentFormat, posHeadOnly: Boolean, user: User
        ): CorpusExport = CorpusExport(
            corpus = corpus,
            job = corpus.jobs.readOrThrow(jobName),
            user = user,
            targetFormat = format,
            posHeadOnly = posHeadOnly
        )
    }
}