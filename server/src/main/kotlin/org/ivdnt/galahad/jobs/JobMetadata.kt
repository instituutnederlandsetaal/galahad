package org.ivdnt.galahad.jobs

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.data.layer.plus
import org.ivdnt.galahad.jobs.Job
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.tagset.Tagset

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
            val djs = job.documentJobs.readAll()
            // sum up the number of tokens/lemmas/etc of all documents
            val resultSummary: LayerSummary = djs.mapNotNull { it.layer?.summary }.reduceOrNull { a, b -> a + b } ?: LayerSummary()
            // Preview of the resulting terms of this job.
            // Show the first preview of the first document that isn't LayerPreview.EMPTY.
            val preview = djs.firstNotNullOfOrNull { it.layer?.preview } ?: LayerPreview.EMPTY

            // When job.name == SOURCE_LAYER_NAME, calling Tagger.readOrThrow will try to read the tagger from the job metadata
            // But we are building that very metadata right now! Instead, we create a dummy tagger.
            val tagger = if (job.name == SOURCE_LAYER_NAME) {
                Tagger.createSourceTagger(job.corpus)
            } else {
                Tagger.readOrThrow(job.name, job.corpus)
            }

            return JobMetadata(
                tagger = tagger,
                progress = job.progress,
                preview = preview,
                resultSummary = resultSummary,
                lastModified = System.currentTimeMillis()
            )
        }
    }
}