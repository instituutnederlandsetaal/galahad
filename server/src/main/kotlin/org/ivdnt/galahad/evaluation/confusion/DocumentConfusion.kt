package org.ivdnt.galahad.evaluation.confusion

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.evaluation.EvaluationEntry
import org.ivdnt.galahad.evaluation.comparison.LayerComparison

/** Annotation confusion table of a document for two different tagger layers. */
class DocumentConfusion(
    @JsonValue
    val confusion: Map<Annotation, Map<String, Map<String, EvaluationEntry>>>
) {
    companion object {
        private val ANNOTATIONS = arrayOf(Annotation.POS, Annotation.UPOS, Annotation.NER, Annotation.DEPREL)

        fun create(layerComparison: LayerComparison): DocumentConfusion = DocumentConfusion(
            buildMap<Annotation, MutableMap<String, MutableMap<String, EvaluationEntry>>> {
                    layerComparison.matches.forEach { termComparison ->
                        termComparison.ref.annotations.filter { it.key in ANNOTATIONS }.forEach { (annotation, _) ->
                            val reference = termComparison.ref.annotationHeadOrMissing(annotation)
                            val hypothesis = termComparison.hyp.annotationHeadOrMissing(annotation)
                            val entry = EvaluationEntry(1, mutableListOf(termComparison))

                            val entryMap = mutableMapOf(hypothesis to entry)
                            val groupedMap = mutableMapOf(reference to entryMap)

                            merge(annotation, groupedMap) { oldAnnotationMap, _ ->
                                oldAnnotationMap.apply {
                                    merge(reference, entryMap) { oldEntry, _ ->
                                        oldEntry.apply {
                                            merge(hypothesis, entry) { e1, e2 -> EvaluationEntry.add(e1, e2) }
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        )
    }
}