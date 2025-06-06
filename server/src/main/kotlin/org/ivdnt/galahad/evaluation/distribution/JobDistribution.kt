package org.ivdnt.galahad.evaluation.distribution

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.util.ThreadPoolUtil
import java.util.concurrent.ExecutorCompletionService

/**
 * The frequency distribution of terms in a corpus for a specific tagger layer.
 * A CorpusDistribution is the sum of the [DocumentDistribution]s of all documents in the corpus.
 */
class JobDistribution(
    corpus: Corpus,
    hypothesis: String = SOURCE_LAYER_NAME,
    groupingAnnotation: Annotation,
) : Distribution(groupingAnnotation) {

    private val hypothesisJob = corpus.jobs.readOrThrow(hypothesis)

    @JsonProperty
    val modified: Long = hypothesisJob.modified

    @JsonProperty
    val generated: Long = System.currentTimeMillis()

    init {
        val completionService = ExecutorCompletionService<DocumentDistribution>(ThreadPoolUtil.pool)

        val allDocs = corpus.documents.readAll()
        allDocs.forEach { doc ->
            completionService.submit {
                DocumentDistribution(hypothesisJob.getLayer(doc), doc.metadata, groupingAnnotation)
            }
        }

        for (i in 0..<allDocs.size) add(completionService.take().get())
    }
}

