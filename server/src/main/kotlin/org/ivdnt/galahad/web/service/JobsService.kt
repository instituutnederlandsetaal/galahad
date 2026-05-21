package org.ivdnt.galahad.web.service

import java.util.UUID
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.LayerAnnotations
import org.ivdnt.galahad.annotations.LayerPreview
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.jobs.Progress
import org.ivdnt.galahad.layers.CorpusLayerMetadata
import org.ivdnt.galahad.taggers.Tagger
import org.springframework.stereotype.Service

@Service
class JobsService(private val corpora: CorporaService) : Logging {
    fun readAll(corpus: UUID, user: User): List<CorpusLayerMetadata> {
        // Create a map of all taggers with empty metadata
        val corpus = corpora.readOrThrow(corpus, user)
        val numDocs = corpus.statistics.numDocs
        val allJobs =
            Tagger.taggers.mapValues {
                CorpusLayerMetadata(
                    it.value,
                    Progress(numDocs),
                    LayerPreview.EMPTY,
                    LayerAnnotations.EMPTY,
                    0,
                )
            }
        // replace the entries for which a job exists
        val jobs = corpus.jobs.readAll().map { it.metadata }.associateBy { it.tagger.name }
        // Replacement is simply plus
        return (allJobs + jobs).values.toList()
    }

    fun readOrThrow(corpus: UUID, job: String, user: User): CorpusLayerMetadata =
        corpora.readOrThrow(corpus, user).jobs.readOrThrow(job).metadata

    fun createOrThrow(corpus: UUID, job: String, user: User): CorpusLayerMetadata =
        corpora.writeOrThrow(corpus, user).jobs.createOrThrow(job).metadata

    fun deleteOrThrow(corpus: UUID, job: String, user: User) {
        corpora.writeOrThrow(corpus, user).jobs.deleteOrThrow(job)
    }
}
