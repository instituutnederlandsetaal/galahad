package org.ivdnt.galahad.evaluation.entities

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.DocumentEvaluations

class JobEntities(
    val documents: Map<String, DocumentEntities>,
    val summary: Map<String, Int>,
    val total: Int,
) {
    companion object {
        fun create(corpus: Corpus, docEvals: DocumentEvaluations): JobEntities {
            val documents: Map<String, DocumentEntities> =
                corpus.documents
                    .readAllSequence()
                    .map { it.name }
                    .associateWith { docEvals.createOrThrow(it).entities }
            val summary: Map<String, Int> =
                documents.values
                    .flatMap { it.entities }
                    .groupBy { it.label }
                    .mapValues { it.value.sumOf { entity -> entity.count } }
            val total: Int = summary.values.sum()
            return JobEntities(documents, summary, total)
        }
    }
}
