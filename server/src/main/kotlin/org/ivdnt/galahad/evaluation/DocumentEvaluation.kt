package org.ivdnt.galahad.evaluation

import java.io.File
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.Documents
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.confusion.DocumentConfusion
import org.ivdnt.galahad.evaluation.distribution.DocumentDistribution
import org.ivdnt.galahad.evaluation.entities.DocumentEntities
import org.ivdnt.galahad.evaluation.metrics.DocumentMetric
import org.ivdnt.galahad.evaluation.spans.DocumentSpanEvaluation
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue

/**
 * Defines evaluations at the level of a document, i.e. where a single document hypothesis is
 * compared to a reference. Or only the reference layer is considered, in which case the JobPair is
 * (reference, reference). Typically, these are only invalidated when the document itself is
 * modified.
 */
class DocumentEvaluation(dir: File, private val corpus: Corpus, private val jobs: JobPair) :
    GalahadFolder(dir) {
    private val referenceDocuments: Documents
        get() = corpus.layers.readOrThrow(jobs.reference)

    private val hypothesisDocuments: Documents
        get() = corpus.layers.readOrThrow(jobs.hypothesis)

    private val refLayer: Layer
        get() = referenceDocuments.readOrThrow(name).layer

    private val hypLayer: Layer
        get() = hypothesisDocuments.readOrThrow(name).layer

    private val refModified: Long
        get() = referenceDocuments.readOrThrow(name).modified

    private val hypModified: Long
        get() = hypothesisDocuments.readOrThrow(name).modified

    private val lastModified: Long
        get() = maxOf(hypModified, refModified)

    private val availableAnnotations: Set<Annotation>
        get() =
            referenceDocuments.metadata.annotations.annotations.keys
                .intersect(hypothesisDocuments.metadata.annotations.annotations.keys)
                .filter { it != Annotation.TOKEN }
                .toSet()

    val entities: DocumentEntities
        get() =
            object : ValidatedDiskValue<DocumentEntities>(dir.resolve(ENTITIES_FILE)) {
                    override fun isValid(modified: Long) = modified >= lastModified

                    override fun set(): DocumentEntities = DocumentEntities.create(refLayer)
                }
                .readOrCreate<DocumentEntities>()

    val distribution: DocumentDistribution
        get() =
            object : ValidatedDiskValue<DocumentDistribution>(dir.resolve(DISTRIBUTION_FILE)) {
                    override fun isValid(modified: Long) = modified >= lastModified

                    override fun set(): DocumentDistribution = DocumentDistribution.create(refLayer)
                }
                .readOrCreate<DocumentDistribution>()

    val confusion: DocumentConfusion
        get() =
            object : ValidatedDiskValue<DocumentConfusion>(dir.resolve(CONFUSION_FILE)) {
                    override fun isValid(modified: Long) = modified >= lastModified

                    override fun set(): DocumentConfusion =
                        DocumentConfusion.create(
                            LayerComparison(hypLayer, refLayer, jobs.filter),
                            availableAnnotations,
                        )
                }
                .readOrCreate<DocumentConfusion>()

    val metrics: DocumentMetric
        get() =
            object : ValidatedDiskValue<DocumentMetric>(dir.resolve(METRICS_FILE)) {
                    override fun isValid(modified: Long) = modified >= lastModified

                    override fun set(): DocumentMetric =
                        DocumentMetric.create(
                            LayerComparison(hypLayer, refLayer, jobs.filter),
                            availableAnnotations,
                        )
                }
                .readOrCreate<DocumentMetric>()

    val spans: DocumentSpanEvaluation
        get() =
            object : ValidatedDiskValue<DocumentSpanEvaluation>(dir.resolve(SPANS_FILE)) {
                    override fun isValid(modified: Long) = modified >= lastModified

                    override fun set(): DocumentSpanEvaluation =
                        DocumentSpanEvaluation.create(
                            LayerComparison(hypLayer, refLayer, jobs.filter),
                            refLayer,
                        )
                }
                .readOrCreate<DocumentSpanEvaluation>()

    companion object {
        private const val ENTITIES_FILE = "entities.json"
        private const val DISTRIBUTION_FILE = "distribution.json"
        private const val CONFUSION_FILE = "confusion.json"
        private const val METRICS_FILE = "metrics.json"
        private const val SPANS_FILE = "spans.json"
    }
}
