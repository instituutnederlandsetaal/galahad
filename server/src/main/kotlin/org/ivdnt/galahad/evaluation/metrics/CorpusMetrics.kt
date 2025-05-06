package org.ivdnt.galahad.evaluation.metrics

import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.comparison.LayerFilter
import org.ivdnt.galahad.jobs.Job
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.ThreadPoolUtil
import java.util.concurrent.ExecutorCompletionService

/**
 * The benchmark [Metric]s of a corpus for two different tagger layers.
 * A CorpusMetrics is the sum of the [DocumentMetrics]s of all documents in the corpus.
 */
class CorpusMetrics(
    corpus: Corpus,
    settings: List<MetricsSettings>,
    hypothesis: String,
    reference: String = SOURCE_LAYER_NAME,
    layerFilter: LayerFilter? = null,
    truncate: Boolean = true,
    hypoTagger2: Tagger = Tagger.readOrThrow(hypothesis, corpus),
    refTagger2: Tagger = Tagger.readOrThrow(reference, corpus),
    hypothesisJob2: Job = corpus.jobs.readOrThrow(hypothesis),
    referenceJob2: Job = corpus.jobs.readOrThrow(reference),
) : Metrics(settings, hypoTagger2, refTagger2, hypothesisJob2, referenceJob2, truncate = truncate) {
    val hypothesisLastModified: Long = hypothesisJob.lastModified

    val referenceLastModified: Long = referenceJob.lastModified

    val generated: Long = System.currentTimeMillis()

    init {
        val completionService = ExecutorCompletionService<DocumentMetrics>(ThreadPoolUtil.pool)

        val allDocs = corpus.documents.readAll()
        allDocs.forEach {
            completionService.submit {
                DocumentMetrics(
                    it, hypoTagger, refTagger, hypothesisJob, referenceJob, settings, layerFilter, truncate
                )
            }
        }

        for (i in 0..<allDocs.size) add(completionService.take().get())
    }
}