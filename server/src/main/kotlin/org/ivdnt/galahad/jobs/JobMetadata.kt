package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.taggers.Tagger

class JobMetadata(val tagger: Tagger, val progress: Progress, val modified: Long = 0) {
    companion object {
        fun create(job: Job, corpus: Corpus): JobMetadata =
            JobMetadata(
                Tagger.readOrThrow(job.name),
                Progress.create(job, corpus),
                System.currentTimeMillis(),
            )
    }
}
