package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.annotations.LayerAnnotations
import org.ivdnt.galahad.annotations.LayerPreview
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.annotations.plus
import org.ivdnt.galahad.taggers.Tagger

/**
 * Cache-able job metadata.
 */
class JobMetadata(
    val tagger: Tagger,
    val progress: Progress,
    val preview: LayerPreview,
    val annotations: LayerAnnotations,
    var modified: Long,
) {
    companion object {
        fun create(job: Job): JobMetadata {
            val djs = job.results.readAll()
            // sum up the number of tokens/lemmas/etc of all documents
            val summary: LayerAnnotations =
                djs.mapNotNull { it.layer?.summary }.reduceOrNull { a, b -> a + b } ?: LayerAnnotations.EMPTY
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
                annotations = summary,
                modified = System.currentTimeMillis()
            )
        }
    }
}