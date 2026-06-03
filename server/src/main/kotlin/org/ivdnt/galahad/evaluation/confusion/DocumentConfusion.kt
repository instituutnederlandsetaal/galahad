package org.ivdnt.galahad.evaluation.confusion

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.EvaluationEntry
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.TermComparison

/** Annotation confusion table of a document for two different tagger layers. */
class DocumentConfusion(@JsonValue val confusion: Map<String, Map<String, EvaluationEntry>>) {
    companion object {
        fun create(
            layerComparison: LayerComparison,
            annotation: Annotation,
        ): DocumentConfusion =
            DocumentConfusion(
                buildMap<String, MutableMap<String, EvaluationEntry>> {
                    layerComparison.matches.forEach { comparison ->
                        val reference = comparison.ref.format(annotation)
                        val hypothesis = comparison.hyp.format(annotation)
                        val entry = EvaluationEntry(1, mutableListOf(comparison))

                        val entryMap = mutableMapOf(hypothesis to entry)

                        this.merge(reference, entryMap) { oldEntry, _ ->
                            oldEntry.apply {
                                this.merge(hypothesis, entry) { e1, e2 ->
                                    EvaluationEntry.add(
                                        e1,
                                        e2,
                                        truncate = layerComparison.filter == null,
                                    )
                                }
                            }
                        }
                    }
                }
            )

        /**
         * Format annotation for confusion matrix. Handle empty terms, empty annotations, and
         * multiple analyses.
         */
        private fun Term.format(annotation: Annotation): String =
            if (this == Term.EMPTY) TermComparison.MISSING_MATCH
            else if (isMulti(annotation)) "MULTIPLE" else annotationHeadOrMissing(annotation)
    }
}
