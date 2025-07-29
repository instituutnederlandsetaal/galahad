package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.distribution.DocumentDistribution
import org.ivdnt.galahad.evaluation.entities.DocumentEntities
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import java.io.File

/**
 * Defines evaluations at the level of a document, i.e. where a single document hypothesis is compared to a reference.
 * Or only the reference layer is considered, in which case the JobPair is (reference, reference).
 * Typically, these are only invalidated when the document itself is modified.
 */
class DocumentEvaluation(
    dir: File,
    private val corpus: Corpus,
    private val jobs: JobPair,
) : GalahadFolder(dir) {
    val refLayer: Layer get() = corpus.jobs.readOrThrow(jobs.reference).getLayer(name)
    val hypLayer: Layer get() = corpus.jobs.readOrThrow(jobs.hypothesis).getLayer(name)

    val entities: DocumentEntities get() = object : ValidatedDiskValue<DocumentEntities>(dir.resolve(ENTITIES_FILE)) {
        override fun isValid(modified: Long) = modified >= corpus.documents.readOrThrow(name).modified
        override fun set(): DocumentEntities = DocumentEntities.create(refLayer)
    }.readOrCreate<DocumentEntities>()

    val distribution: DocumentDistribution get() = object : ValidatedDiskValue<DocumentDistribution>(dir.resolve(DISTRIBUTION_FILE)) {
        override fun isValid(modified: Long) = modified >= corpus.documents.readOrThrow(name).modified
        override fun set(): DocumentDistribution = DocumentDistribution.create(refLayer)
    }.readOrCreate<DocumentDistribution>()

    companion object {
        private const val ENTITIES_FILE = "entities.json"
        private const val DISTRIBUTION_FILE = "distribution.json"
    }
}