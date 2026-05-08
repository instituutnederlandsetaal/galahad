package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.confusion.JobConfusion
import org.ivdnt.galahad.evaluation.distribution.JobDistribution
import org.ivdnt.galahad.evaluation.entities.JobEntities
import org.ivdnt.galahad.evaluation.metrics.JobMetric
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import org.ivdnt.galahad.jobs.Job
import java.io.File

/**
 * Defines evaluations at the level of a job pair, i.e. where all documents of a hypothesis is
 * compared to a reference.
 */
class JobEvaluation(dir: File, private val corpus: Corpus, private val jobs: JobPair) :
    GalahadFolder(dir) {
    val refJob: Job
        get() = corpus.jobs.readOrThrow(jobs.reference)

    val hypJob: Job
        get() = corpus.jobs.readOrThrow(jobs.hypothesis)

    /** Access evaluations of individual documents in this job pair. */
    val documents: DocumentEvaluations =
        DocumentEvaluations(dir.resolve(DOCUMENTS_FOLDER), corpus, jobs)

    val entities: JobEntities
        get() =
            object : ValidatedDiskValue<JobEntities>(dir.resolve(ENTITIES_FILE)) {
                    override fun isValid(modified: Long) =
                        modified >= maxOf(refJob.modified, hypJob.modified)

                    override fun set(): JobEntities = JobEntities.create(corpus, documents)
                }
                .readOrCreate()

    val distribution: JobDistribution
        get() =
            object : ValidatedDiskValue<JobDistribution>(dir.resolve(DISTRIBUTION_FILE)) {
                    override fun isValid(modified: Long) =
                        modified >= maxOf(refJob.modified, hypJob.modified)

                    override fun set(): JobDistribution = JobDistribution.create(corpus, documents)
                }
                .readOrCreate()

    val confusion: JobConfusion
        get() =
            object : ValidatedDiskValue<JobConfusion>(dir.resolve(CONFUSION_FILE)) {
                    override fun isValid(modified: Long) =
                        modified >= maxOf(refJob.modified, hypJob.modified)

                    override fun set(): JobConfusion = JobConfusion.create(corpus, documents)
                }
                .readOrCreate()

    val metrics: JobMetric
        get() =
            object : ValidatedDiskValue<JobMetric>(dir.resolve(METRICS_FILE)) {
                    override fun isValid(modified: Long) =
                        modified >= maxOf(refJob.modified, hypJob.modified)

                    override fun set(): JobMetric = JobMetric.create(corpus, documents)
                }
                .readOrCreate()

    companion object {
        private const val DISTRIBUTION_FILE = "distribution.json"
        private const val ENTITIES_FILE = "entities.json"
        private const val CONFUSION_FILE = "confusion.json"
        private const val METRICS_FILE = "metrics.json"
        private const val DOCUMENTS_FOLDER = "documents"
    }
}
