package org.ivdnt.galahad.evaluation.confusion

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.evaluation.CsvSampleExporter
import org.ivdnt.galahad.evaluation.comparison.LayerFilter
import org.ivdnt.galahad.taggers.Tagger


/**
 * Part of speech confusion of a corpus for two different tagger layers.
 * A CorpusConfusion is the sum of the [DocumentConfusion]s of all documents in the corpus.
 */
class CorpusConfusion(
    corpus: Corpus,
    val hypothesis: String,
    val reference: String = SOURCE_LAYER_NAME,
    annotation: AnnotationType = AnnotationType.POS,
    layerFilter: LayerFilter? = null,
) : Confusion(truncate = layerFilter == null, annotation), CsvSampleExporter {

    private val hypothesisJob = corpus.jobs.readOrThrow(hypothesis)
    private val referenceJob = corpus.jobs.readOrThrow(reference)
    private val refTagger = Tagger.readOrThrow(reference, corpus)
    private val hypoTagger = Tagger.readOrThrow(hypothesis, corpus)

    @JsonProperty
    val hypothesisLastModified = hypothesisJob.lastModified

    @JsonProperty
    val referenceLastModified = referenceJob.lastModified

    @JsonProperty
    val generated = System.currentTimeMillis()

    init {
        corpus.documents.readAll().forEach {
            add(
                DocumentConfusion(
                    hypothesisJob.layer(it),
                    referenceJob.layer(it),
                    layerFilter,
                    annotation,
                )
            )
        }
    }

    /**
     * CSV representation of all samples where the hypothesis pos and reference pos are [hypoPos] and [refPos].
     */
    override fun samplesToCSV(): String {
        return samplesToCSV(matrix.values.firstOrNull()?.samples, hypoTagger, refTagger)
    }
}