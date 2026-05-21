package org.ivdnt.galahad.evaluation.frequency

import org.ivdnt.galahad.corpora.Corpus

class TokenFrequency(corpus: Corpus, layerName: String) {
    private val tokenFrequency: Map<String, Int>

    init {
        val map = mutableMapOf<String, Int>()
        val layers = corpus.layers.readOrThrow(layerName)

        layers.documents.readAll().forEach {
            it.layer.terms.forEach { t ->
                val token = t.token.lowercase()
                map[token] = map.getOrDefault(token, 0) + 1
            }
        }

        tokenFrequency = map.toMap()
    }

    fun getFrequency(token: String): Int = tokenFrequency.getOrDefault(token, 0)
}
