package org.ivdnt.galahad.evaluation.distribution

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.annotations.AnnotationType
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.corpora.Corpus

/**
 * The frequency distribution of terms in a corpus for a specific tagger layer.
 * A CorpusDistribution is the sum of the [DocumentDistribution]s of all documents in the corpus.
 */
class CorpusDistribution(
    corpus: Corpus,
    hypothesis: String = SOURCE_LAYER_NAME,
    groupingAnnotation: AnnotationType,
) : Distribution(groupingAnnotation) {

    private val hypothesisJob = corpus.jobs.readOrThrow(hypothesis)

    @JsonProperty
    val lastModified: Long = hypothesisJob.lastModified

    @JsonProperty
    val generated: Long = System.currentTimeMillis()

    init {
        corpus.documents.readAll().forEach {
            val layer = hypothesisJob.layer(it)
            // Add to ourselves
            this.add(DocumentDistribution(layer, it.metadata, groupingAnnotation))
        }
    }
}

