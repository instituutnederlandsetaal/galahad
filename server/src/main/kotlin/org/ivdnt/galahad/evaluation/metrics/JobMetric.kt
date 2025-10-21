package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.DocumentEvaluations
import org.ivdnt.galahad.util.merge

class JobMetric(
    @JsonValue
    val classesByGroup: MutableMap<String, NewMetric>
) {
    companion object {
        fun create(corpus: Corpus, docEvals: DocumentEvaluations): JobMetric = JobMetric(
            corpus.documents.readAllSequence().map { docEvals.createOrThrow(it.name).metrics.classesByGroup }
                .reduce { map1, map2 ->
                    map1.merge(map2) { a, b -> a.apply { grouped.merge(b.grouped) { x, y -> x.add(y) } } } as MutableMap<String, NewMetric>
                }
        )
    }
}