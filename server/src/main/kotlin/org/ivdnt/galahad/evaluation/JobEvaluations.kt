package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.confusion.CONFUSION_TYPES
import org.ivdnt.galahad.evaluation.distribution.CorpusDistribution
import org.ivdnt.galahad.exceptions.DocumentNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import org.ivdnt.galahad.files.ValidatedDiskValue
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.web.controller.DISTRIBUTION_MAX_SIZE
import java.io.File

class JobEvaluations(
    dir: File,
    private val corpus: Corpus,
    private val jobs: JobPair,
) : GalahadFolderManager<DocumentEvaluations, String>(dir) {
    override fun ctor(key: String): DocumentEvaluations {
        TODO("Not yet implemented")
    }

    override fun throwNotFound(key: String): Nothing = throw DocumentNotFoundException(key)


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
    }

}