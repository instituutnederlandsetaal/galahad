package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.layers.CorpusLayerMetadata
import org.ivdnt.galahad.taggers.Tagger

class JobMetadata(val layer: CorpusLayerMetadata, val progress: Progress) {
    companion object {
        fun create(job: Job, corpus: Corpus): JobMetadata =
            JobMetadata(
                CorpusLayerMetadata(Tagger.readOrThrow(job.name)),
                Progress.create(job, corpus),
            )
    }
}
