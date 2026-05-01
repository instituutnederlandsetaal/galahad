package org.ivdnt.galahad.evaluation.frequency

import org.ivdnt.galahad.corpora.Corpus

class TokenFrequency(corpus: Corpus, jobName: String) {
    private val tokenFrequency: Map<String, Int>

    init {
        val map = mutableMapOf<String, Int>()
        val job = corpus.jobs.readOrThrow(jobName)

        corpus.documents.readAll().forEach {
            job.getLayer(it).terms.forEach { t ->
                val token = t.token.lowercase()
                map[token] = map.getOrDefault(token, 0) + 1
            }
        }

        tokenFrequency = map.toMap()
    }

    fun getFrequency(token: String): Int = tokenFrequency.getOrDefault(token, 0)
}
