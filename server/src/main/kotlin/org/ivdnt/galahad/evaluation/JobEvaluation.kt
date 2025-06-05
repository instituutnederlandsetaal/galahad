package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.confusion.CONFUSION_TYPES
import org.ivdnt.galahad.evaluation.distribution.CorpusDistribution
import org.ivdnt.galahad.evaluation.entities.DocumentEntities
import org.ivdnt.galahad.evaluation.entities.JobEntities
import org.ivdnt.galahad.exceptions.DocumentNotFoundException
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.GalahadFolderManager
import org.ivdnt.galahad.files.ValidatedDiskValue
import org.ivdnt.galahad.taggers.Tagger
import java.io.File

class JobEvaluation(
    dir: File,
    private val corpus: Corpus,
    private val jobs: JobPair,
) : GalahadFolder(dir) {
    val documents: DocumentEvaluations = DocumentEvaluations(dir.resolve(DOCUMENTS_FOLDER), corpus, jobs)

    val entities: JobEntities get() = entitiesCache.readOrCreate()
    private val entitiesFile = dir.resolve(ENTITIES_FILE)
    private val entitiesCache = object : ValidatedDiskValue<JobEntities>(entitiesFile) {
        override fun isValid(lastModified: Long) = lastModified >= corpus.lastModified
        override fun set(): JobEntities = JobEntities.create(corpus, documents)
    }

    val distribution: Map<Annotation, CorpusDistribution> get() = distributionCache.readOrCreate()
    private val distributionFile = dir.resolve(DISTRIBUTION_FILE)
    private val distributionCache = object : ValidatedDiskValue<Map<Annotation, CorpusDistribution>>(distributionFile) {
        override fun isValid(lastModified: Long) = lastModified >= corpus.lastModified
        override fun set(): Map<Annotation, CorpusDistribution> {
            val allAnnots = Tagger.readOrThrow(jobs.reference, corpus).annotations
            if (Annotation.LEMMA !in allAnnots) {
                return emptyMap()
            }
            val annotationTypes = CONFUSION_TYPES.filter { it in allAnnots }
            val distributions = annotationTypes.associateWith {
                CorpusDistribution(
                    corpus,
                    jobs.reference,
                    it
                ).trim(DISTRIBUTION_MAX_SIZE) as CorpusDistribution
            }
            return distributions
        }
    }

    companion object {
        private const val DISTRIBUTION_FILE = "distribution.json"
        private const val ENTITIES_FILE = "entities.json"
        const val DOCUMENTS_FOLDER = "documents"
        const val DISTRIBUTION_MAX_SIZE: Int = 1000
    }
}