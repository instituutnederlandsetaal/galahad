package org.ivdnt.galahad.web.service

import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.jobs.JobMetadata
import org.ivdnt.galahad.jobs.Progress
import org.ivdnt.galahad.taggers.Tagger
import org.springframework.stereotype.Service

@Service
class JobsService(private val corpora: CorporaService) : Logging {
    fun readAll(corpus: UUID): List<JobMetadata> {
        // Create a map of all taggers with empty metadata
        val corpus = corpora.readOrThrow(corpus)
        val numDocs = corpus.statistics.numDocs
        val allJobs = Tagger.taggers.mapValues { JobMetadata(it.value, Progress(numDocs)) }
        // replace the entries for which a job exists
        val jobs = corpus.jobs.readAll().map { it.metadata }.associateBy { it.tagger.name }
        // Replacement is simply plus
        return (allJobs + jobs).values.toList()
    }

    fun readOrThrow(corpus: UUID, job: String): JobMetadata {
        val tagger = Tagger.readOrThrow(job)
        val corpus = corpora.readOrThrow(corpus)
        val metadata = corpus.jobs.readOrNull(job)?.metadata
        if (metadata == null) {
            val untagged = corpus.documents.readAll().size
            return JobMetadata(tagger, Progress(untagged))
        }
        return metadata
    }

    fun createOrThrow(corpus: UUID, job: String) {
        corpora.writeOrThrow(corpus).jobs.createOrThrow(job)
    }

    fun deleteOrThrow(corpus: UUID, job: String) {
        corpora.writeOrThrow(corpus).jobs.deleteOrThrow(job)
    }
}
