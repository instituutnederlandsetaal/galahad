package org.ivdnt.galahad.evaluation.confusion

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.DocumentEvaluations
import org.ivdnt.galahad.evaluation.EvaluationEntry
import org.ivdnt.galahad.util.merge

/**
 * Part of speech confusion of a corpus for two different tagger layers.
 * A CorpusConfusion is the sum of the [DocumentConfusion]s of all documents in the corpus.
 */
class JobConfusion(
    @JsonValue
    val confusion: Map<Annotation, Map<String, Map<String, EvaluationEntry>>>
) {
    companion object {
        fun create(corpus: Corpus, docEvals: DocumentEvaluations): JobConfusion = JobConfusion(
            corpus.documents.readAllSequence().map { docEvals.createOrThrow(it.name).confusion.confusion }
                .reduce { map1, map2 ->
                    map1.merge(map2) { v1, v2 ->
                        v1.merge(v2) { m1, m2 ->
                            m1.merge(m2) { e1, e2 ->
                                EvaluationEntry.add(e1, e2)
                            }
                        }
                    }
                }
        )
    }
}