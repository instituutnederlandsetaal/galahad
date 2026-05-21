package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.taggers.Tagger

class JobMetadata(val tagger: Tagger, val progress: Progress) {
    companion object {
        fun create(job: Job, corpus: Corpus): JobMetadata {
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
            if (JobController.inQueue(job)) {
                processing = untagged
                untagged = 0
            }
            val progress =
                Progress(
                    untagged = untagged,
                    processing = processing,
                    failed = statuses.count { it == JobStatus.ERROR },
                    finished = statuses.count { it == JobStatus.FINISHED },
                    errors = errors,
                )
            return JobMetadata(tagger = Tagger.readOrThrow(job.name), progress = progress)
        }
    }
}
