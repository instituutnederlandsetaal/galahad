package org.ivdnt.galahad.jobs

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.data.layer.plus
import org.ivdnt.galahad.jobs.Job
import org.ivdnt.galahad.taggers.Tagger

/**
 * Cache-able job metadata.
 */
class JobMetadata(
    @JsonProperty("tagger") val tagger: Tagger = Tagger(),
    @JsonProperty("progress") val progress: Progress = Progress(),
    @JsonProperty("preview") val preview: LayerPreview = LayerPreview(),
    @JsonProperty("resultSummary") val resultSummary: LayerSummary = LayerSummary(),
    @JsonProperty("lastModified") var lastModified: Long? = null,
) {
    companion object {
        fun create(job: Job): JobMetadata {
            // sum up the number of tokens/lemmas/etc of all documents
            // This is very expensive
            val resultSummary: LayerSummary =
                job.documentJobs.readAll().mapNotNull { it.layer?.summary }.reduceOrNull { a, b -> a + b } ?: LayerSummary()

            // Preview of the resulting terms of this job.
            // Show the first preview of the first document that isn't LayerPreview.EMPTY.
            val preview = job.documentJobs.readAll().firstNotNullOfOrNull { it.layer?.preview } ?: LayerPreview.EMPTY

            return JobMetadata(
                tagger = job.taggerStore.getSummaryOrThrow(job.name, job.corpus.sourceTagger).expensiveGet(),
                progress = job.progress,
                preview = preview,
                resultSummary = resultSummary,
                lastModified = System.currentTimeMillis()
            )
        }
    }
}