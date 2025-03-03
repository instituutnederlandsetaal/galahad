package org.ivdnt.galahad.evaluation.frequency

import org.ivdnt.galahad.data.corpus.Corpus

class TokenFrequency(
    corpus: Corpus,
    jobName: String
) {
    private val tokenFrequency: Map<String, Int>

    init {
        val map = mutableMapOf<String, Int>()
        val job = corpus.jobs.readOrThrow(jobName)

        corpus.documents.readAll().forEach {
            val meta = it.metadata.expensiveGet()
            val documentJob = job.documentOrThrow(meta.name)
            documentJob.result.terms.forEach { t ->
                val token = t.literals.lowercase()
                map[token] = map.getOrDefault(token, 0) + 1
            }
        }

        tokenFrequency = map.toMap()
    }

    fun getFrequency(token: String): Int {
        return tokenFrequency.getOrDefault(token, 0)
    }
}