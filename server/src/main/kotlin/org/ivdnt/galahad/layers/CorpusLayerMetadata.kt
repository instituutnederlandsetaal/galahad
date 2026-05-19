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
    val preview: LayerPreview,
    val annotations: LayerAnnotations,
    var modified: Long,
) {
    companion object {
        fun create(layers: CorpusLayer, corpus: Corpus): CorpusLayerMetadata {
            val tagger =
                if (layers.name == Layer.SOURCE_LAYER) {
                    Tagger.createSourceTagger(corpus)
                } else {
                    Tagger.readOrThrow(layers.name)
                }

            return CorpusLayerMetadata(
                tagger = tagger,
                preview =
                    layers.documents.readAll().firstOrNull()?.layer?.preview ?: LayerPreview.EMPTY,
                annotations =
                    layers.documents
                        .readAll()
                        .takeUnless { it.isEmpty() }
                        ?.map { it.metadata.annotations }
                        ?.reduce { a, b -> a + b } ?: LayerAnnotations.EMPTY,
                modified = System.currentTimeMillis(),
            )
        }
    }
}
