package org.ivdnt.galahad.evaluation.confusion

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.CsvSampleExporter
import org.ivdnt.galahad.evaluation.comparison.LayerFilter
import org.ivdnt.galahad.evaluation.distribution.DocumentDistribution
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.ThreadPoolUtil
import java.util.concurrent.ExecutorCompletionService

/**
 * Part of speech confusion of a corpus for two different tagger layers.
 * A CorpusConfusion is the sum of the [DocumentConfusion]s of all documents in the corpus.
 */
class CorpusConfusion(
    corpus: Corpus,
    val hypothesis: String,
    val reference: String = SOURCE_LAYER_NAME,
    annotation: Annotation = Annotation.POS,
    layerFilter: LayerFilter? = null,
) : Confusion(truncate = layerFilter == null, annotation), CsvSampleExporter {

    private val hypothesisJob = corpus.jobs.readOrThrow(hypothesis)
    private val referenceJob = corpus.jobs.readOrThrow(reference)
    private val refTagger = Tagger.readOrThrow(reference, corpus)
    private val hypoTagger = Tagger.readOrThrow(hypothesis, corpus)

    @JsonProperty
    val hypothesisLastModified: Long = hypothesisJob.lastModified

    @JsonProperty
    val referenceLastModified: Long = referenceJob.lastModified

    @JsonProperty
    val generated: Long = System.currentTimeMillis()

    init {
        val completionService = ExecutorCompletionService<DocumentConfusion>(ThreadPoolUtil.pool)
        val allDocs = corpus.documents.readAll()

        allDocs.forEach {
            completionService.submit {
                DocumentConfusion(
                    hypothesisJob.getLayer(it),
                    referenceJob.getLayer(it),
                    layerFilter,
                    annotation,
                )
            }
        }

        for (i in 0..<allDocs.size) add(completionService.take().get())
    }

    /**
     * CSV representation of all samples where the hypothesis pos and reference pos are [hypoTagger] and [refTagger].
     */
    override fun samplesToCSV(): String = samplesToCSV(matrix.values.firstOrNull()?.samples, hypoTagger, refTagger)
}