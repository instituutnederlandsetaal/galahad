package org.ivdnt.galahad.layers

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.LayerAnnotations
import org.ivdnt.galahad.annotations.LayerAnnotations.Companion.plus
import org.ivdnt.galahad.annotations.LayerPreview
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.taggers.Tagger

/** Cache-able job metadata. */
class CorpusLayerMetadata(
    val tagger: Tagger,
//    val progress: Progress,
    val preview: LayerPreview,
    val annotations: LayerAnnotations,
    var modified: Long,
) {
    companion object {
        fun create(layers: CorpusLayer, corpus: Corpus): CorpusLayerMetadata {
//            val djs = job.results.readAll()
//            // sum up the number of tokens/lemmas/etc of all documents
//            val summary: LayerAnnotations =
//                djs.mapNotNull { it.layer?.summary }.reduceOrNull { a, b -> a + b }
//                    ?: LayerAnnotations.EMPTY
            // Preview of the resulting terms of this job.
            // Show the first preview of the first document that isn't LayerPreview.EMPTY.
//            val preview = djs.firstNotNullOfOrNull { it.layer?.preview } ?: LayerPreview.EMPTY

            // When job.name == SOURCE_LAYER_NAME, calling Tagger.readOrThrow will try to read the
            // tagger
            // from the job metadata
            // But we are building that very metadata right now! Instead, we create a dummy tagger.
            val tagger =
                if (layers.name == Layer.SOURCE_LAYER) {
                    Tagger.createSourceTagger(corpus)
                } else {
                    Tagger.readOrThrow(layers.name)
                }

            return CorpusLayerMetadata(
                tagger = tagger,
//                progress = job.progress,
                preview = layers.documents.readAll().firstOrNull()?.layer?.preview ?: LayerPreview.EMPTY,
                annotations = layers.documents.readAll().takeUnless { it.isEmpty() }?.map { it.metadata.annotations }?.reduce { a, b -> a + b } ?: LayerAnnotations.EMPTY,
                modified = System.currentTimeMillis(),
            )
        }
    }
}
