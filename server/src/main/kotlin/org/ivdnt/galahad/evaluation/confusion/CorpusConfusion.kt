package org.ivdnt.galahad.evaluation.confusion

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.evaluation.CsvSampleExporter
import org.ivdnt.galahad.evaluation.comparison.LayerFilter
import org.ivdnt.galahad.taggers.TaggerStore

/**
 * Part of speech confusion of a corpus for two different tagger layers.
 * A CorpusConfusion is the sum of the [DocumentConfusion]s of all documents in the corpus.
 */
class CorpusConfusion(
    val corpus: Corpus,
    val hypothesis: String,
    val reference: String = SOURCE_LAYER_NAME,
    annotation: AnnotationType = AnnotationType.POS,
    layerFilter: LayerFilter? = null,
) : Confusion(truncate = layerFilter == null, annotation), CsvSampleExporter {

    private val taggerStore: TaggerStore = TaggerStore()

    private val hypothesisJob = corpus.jobs.readOrThrow(hypothesis)
    private val referenceJob = corpus.jobs.readOrThrow(reference)

    @JsonProperty
    val hypothesisLastModified = hypothesisJob.lastModified

    @JsonProperty
    val referenceLastModified = referenceJob.lastModified

    @JsonProperty
    val generated = System.currentTimeMillis()

    init {
        corpus.documents.readAll().forEach {
            val name = it.metadata.expensiveGet().name
            add(
                DocumentConfusion(
                    hypothesisJob.documentOrThrow(name).result,
                    referenceJob.documentOrThrow(name).result,
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
        val refTagger = taggerStore.getSummaryOrThrow(reference, corpus.sourceTagger).expensiveGet()
        val hypoTagger = taggerStore.getSummaryOrThrow(hypothesis, corpus.sourceTagger).expensiveGet()
        return samplesToCSV(matrix.values.firstOrNull()?.samples, hypoTagger, refTagger)
    }
}