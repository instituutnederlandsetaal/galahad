package org.ivdnt.galahad.evaluation.distribution

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.AnnotationType

/**
 * The frequency distribution of terms in a corpus for a specific tagger layer.
 * A CorpusDistribution is the sum of the [DocumentDistribution]s of all documents in the corpus.
 */
class CorpusDistribution(
    corpus: Corpus,
    hypothesis: String = SOURCE_LAYER_NAME,
    annotation: AnnotationType
) : Distribution(annotation) {

    private val hypothesisJob = corpus.jobs.readOrNull(hypothesis) ?: throw Exception("Hypothesis layer does not exist")

    @JsonProperty
    val lastModified = hypothesisJob.lastModified

    @JsonProperty
    val generated = System.currentTimeMillis()

    init {
        corpus.documents.readAll().forEach {
            val meta = it.metadata.expensiveGet()
            val documentJob = hypothesisJob.documentOrThrow(meta.name)
            // Add to ourselves
            this.add(DocumentDistribution(documentJob.result, meta, annotation))
        }
    }
}

