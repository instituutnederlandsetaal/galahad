package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.corpora.Corpus

class Progress(
    val untagged: Int = 0,
    val processing: Int = 0,
    val failed: Int = 0,
    val finished: Int = 0,
    val errors: Map<String, String> = mapOf(), // Map<doc name, error text>
) {
    val total: Int = untagged + processing + failed + finished

    companion object {
        fun create(job: Job, corpus: Corpus): Progress {
            /** Progress of the job based on the status of the [JobResult]s of this job. */
            // NOTE: The number of documents is not the same as the number of document jobs.
            // Example: after running a job, a user has added more documents to the corpus.
            // So for calculating progress, we need to look at the number of corpus documents.
            val docs = corpus.documents.readAll()
            // If a document is not in the list of documentJobs, it is untagged by default.
            val statuses = docs.map {
                job.results.readOrNull(it.name)?.status ?: JobStatus.UNTAGGED
            }
            // For errors however, we can just look at the documentJobs.
            val errors =
                job.results
                    .readAll()
                    .mapNotNull { result -> result.error?.let { error -> result.name to error } }
                    .toMap()
            var untagged = statuses.count { it == JobStatus.UNTAGGED }
            var processing = 0
            if (JobSchedular.inQueue(job)) {
                processing = untagged
                untagged = 0
            }
            return Progress(
                untagged = untagged,
                processing = processing,
                failed = statuses.count { it == JobStatus.ERROR },
                finished = statuses.count { it == JobStatus.FINISHED },
                errors = errors,
            )
        }
    }
}
