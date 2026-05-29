package org.ivdnt.galahad.layers

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.LayerAnnotations
import org.ivdnt.galahad.annotations.LayerAnnotations.Companion.plus
import org.ivdnt.galahad.annotations.LayerPreview
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.taggers.Tagger

/** Cache-able layer metadata. */
class CorpusLayerMetadata(
    val tagger: Tagger,
    val documents: Int = 0,
    val preview: LayerPreview = LayerPreview.EMPTY,
    val annotations: LayerAnnotations = LayerAnnotations.EMPTY,
    var modified: Long = 0,
) {
    companion object {
        fun create(layers: CorpusLayer, corpus: Corpus): CorpusLayerMetadata {
            val tagger =
                if (layers.name == Layer.SOURCE_LAYER) {
                    Tagger.createSourceTagger(corpus)
                } else {
                    Tagger.readOrThrow(layers.name)
                }

            val docs = layers.documents.readAll()
            return CorpusLayerMetadata(
                tagger = tagger,
                documents = docs.size,
                preview = docs.firstOrNull()?.layer?.preview ?: LayerPreview.EMPTY,
                annotations =
                    docs
                        .takeUnless { it.isEmpty() }
                        ?.map { it.metadata.annotations }
                        ?.reduce { a, b -> a + b } ?: LayerAnnotations.EMPTY,
                modified = System.currentTimeMillis(),
            )
        }
    }
}
