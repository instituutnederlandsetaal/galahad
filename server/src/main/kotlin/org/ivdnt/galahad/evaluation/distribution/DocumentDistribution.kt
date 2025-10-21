package org.ivdnt.galahad.evaluation.distribution

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer

/** Type-token distribution of terms in a layer. */
class DocumentDistribution(
    @JsonValue val typeTokens: Map<Annotation, List<TypeToken>>
) {
    companion object {
        private val ANNOTATIONS = arrayOf(Annotation.POS, Annotation.UPOS, Annotation.NER, Annotation.DEPREL)

        fun create(layer: Layer): DocumentDistribution =
            DocumentDistribution(buildMap<Annotation, MutableMap<Pair<String, String>, MutableMap<String, Int>>> {
                // We're going to construct a map for each annotation,
                // with key [lemma; that annotation head] and [tokens; their counts] as values.
                // such that we can group by lemma and annotation head.
                // Then convert it to List<TypeToken>.
                layer.terms.forEach { t ->
                    // For each term in the layer, get the valid annotations and their values
                    t.annotations.filter { it.key in ANNOTATIONS }.forEach { (annotation, _) ->
                        // map the annotation to a map of pairs [lemma; annotation head] and tokens and their counts.
                        // Pair examples: [NO_LEMMA; PC], [lopen; VRB] (so head only)
                        val pair = t.annotationOrMissing(Annotation.LEMMA) to t.annotationHeadOrMissing(annotation)
                        val tokens = mutableMapOf(t.token to 1)
                        merge(annotation, mutableMapOf(pair to tokens)) { oldTypeTokens, newTypeTokens ->
                            // merge this pair-tokencount mapping if needed
                            oldTypeTokens.apply {
                                this.merge(pair, tokens) { oldTokens, newTokens ->
                                    // In doing so, we only need to sum the token counts
                                    oldTokens.apply {
                                        this.merge(t.token, 1, Integer::sum)
                                    }
                                }
                            }
                        }
                    }
                }
            }.mapValues { (_, group) ->
                // convert key [lemma; that annotation head] and value [tokens; their counts]
                // to simply TypeToken(lemma, group, tokens).
                group.map { (k, v) ->
                    TypeToken(
                        lemma = k.first, group = k.second, tokens = v
                    )
                }.sortedByDescending { it.count } // nice to have it sorted on count
            })

    }
}