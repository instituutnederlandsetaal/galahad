package org.ivdnt.galahad.corpora

import java.util.*

/**
 * Metadata about a corpus, to be stored in a cache file, as its immutable fields can become
 * invalidated (e.g. [documents], [modified]), and of course any [CorpusMetadata] field. This is the
 * superset of [CorpusMetadata], and contains, in addition to the mutable fields of the latter, any
 * immutable fields like [size]. [size] is expensive to calculate, hence the cache file.
 */
class CorpusStatistics(
    // Mutable fields
    name: String,
    owner: String?,
    period: Period?,
    language: String?,
    tagset: String?,
    dataset: Boolean?,
    collaborators: MutableSet<String>?,
    viewers: MutableSet<String>?,
    source: Source?,
    // Immutable fields
    val uuid: UUID,
    val jobs: Int,
    val processing: Int,
    val documents: Int,
    val size: Long,
    val modified: Long,
) :
    CorpusMetadata(
        name = name,
        owner = owner,
        dataset = dataset,
        period = period,
        language = language,
        tagset = tagset,
        source = source,
        collaborators = collaborators,
        viewers = viewers,
    ) {
    companion object {
        fun create(corpus: Corpus): CorpusStatistics =
            CorpusStatistics(
                // Mutable fields
                name = corpus.metadata.name,
                owner = corpus.metadata.owner,
                dataset = corpus.metadata.dataset,
                period = corpus.metadata.period,
                language = corpus.metadata.language,
                tagset = corpus.metadata.tagset,
                source = corpus.metadata.source,
                collaborators = corpus.metadata.collaborators,
                viewers = corpus.metadata.viewers,
                // Immutable fields
                uuid = corpus.uuid,
                jobs = corpus.jobs.readAll().size,
                processing = corpus.jobs.readAll().count { it.metadata.progress.processing > 0 },
                documents = corpus.documents.readAll().size,
                size = corpus.size,
                modified = System.currentTimeMillis(),
            )
    }
}
