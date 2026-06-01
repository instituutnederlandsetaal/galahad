package org.ivdnt.galahad.web.service

import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.jobs.JobController
import org.ivdnt.galahad.jobs.JobMetadata
import org.ivdnt.galahad.jobs.Progress
import org.ivdnt.galahad.taggers.Tagger
import org.springframework.stereotype.Service

@Service
class JobsService(private val corpora: CorporaService) : Logging {
    fun readAll(corpus: UUID): List<JobMetadata> {
        // Create a map of all taggers with empty metadata
        val corpus = corpora.readOrThrow(corpus)
        val numDocs = corpus.statistics.documents
        val allJobs =
            Tagger.taggers.mapValues {
                JobMetadata(it.value, Progress(numDocs))
            }
        // replace the entries for which a job exists
        val jobs = corpus.jobs.readAll().map { it.metadata }.associateBy { it.tagger.name }
        // Replacement is simply plus
        return (allJobs + jobs).values.toList()
    }

    // separate function because we want to avoid reading expensive layer metadata
    fun readOrThrowProgress(corpus: UUID, job: String): Progress =
        corpora.readOrThrow(corpus).let { Progress.create(it.jobs.readOrThrow(job), it) }

    fun readOrThrow(corpus: UUID, job: String): JobMetadata {
        val corpus = corpora.readOrThrow(corpus)
        val tagger = Tagger.readOrThrow(job)
        return corpus.jobs.readOrNull(job)?.metadata
            ?: JobMetadata(tagger, Progress(corpus.statistics.documents))
    }

    fun createOrThrow(corpus: UUID, job: String) {
        corpora.writeOrThrow(corpus).jobs.createOrThrow(job)
    }

    fun deleteOrThrow(corpus: UUID, job: String) {
        // does it exist?
        val job = corpora.writeOrThrow(corpus).jobs.readOrThrow(job)
        // stop it
        JobController.dequeue(job)
    }
}
