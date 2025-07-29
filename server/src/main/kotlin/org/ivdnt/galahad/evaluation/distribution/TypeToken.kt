package org.ivdnt.galahad.evaluation.distribution

class TypeToken(
    val lemma: String, val group: String, val tokens: Map<String, Int>, val count: Int = tokens.values.sum()
)