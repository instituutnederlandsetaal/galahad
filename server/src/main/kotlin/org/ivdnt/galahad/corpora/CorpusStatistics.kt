package org.ivdnt.galahad.corpora

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL
import java.util.*

/**
 * Metadata about a corpus, to be stored in a cache file, as its immutable fields can become
 * invalidated (e.g. [numDocs], [modified]), and of course any [CorpusMetadata] field. This is the
 * superset of [CorpusMetadata], and contains, in addition to the mutable fields of the latter, any
 * immutable fields like [size]. [size] is expensive to calculate, hence the cache file.
 */
class CorpusStatistics(
    // Mutable fields
    @JsonProperty("owner") owner: String = "",
    @JsonProperty("name") name: String = "",
    @JsonProperty("eraTo") eraTo: Int = 0,
    @JsonProperty("eraFrom") eraFrom: Int = 0,
    @JsonProperty("language") language: String? = null,
    @JsonProperty("tagset") tagset: String? = null,
    @JsonProperty("dataset") dataset: Boolean = false,
    @JsonProperty("collaborators") collaborators: Set<String> = setOf(),
    @JsonProperty("viewers") viewers: Set<String> = setOf(),
    @JsonProperty("sourceName") sourceName: String? = null,
    @JsonProperty("sourceURL") sourceURL: URL? = null,
    // Immutable fields
    val uuid: UUID = UUID(0, 0),
    val activeJobs: Int = 0,
    val numResults: Int = 0,
    val numDocs: Int = 0,
    val size: Long = 0,
    val modified: Long = 0,
) :
    CorpusMetadata(
        name = name,
        owner = owner,
        eraFrom = eraFrom,
        eraTo = eraTo,
        language = language,
        tagset = tagset,
        dataset = dataset,
        collaborators = collaborators.toMutableSet(),
        viewers = viewers.toMutableSet(),
        sourceName = sourceName,
        sourceURL = sourceURL,
    ) {
    companion object {
        fun create(corpus: Corpus): CorpusStatistics {
            val meta =
                CorpusStatistics(
                    // Immutable/calculated fields
                    // sourceAnnotationTypes = uniqueAnnotations,
                    uuid = UUID.fromString(corpus.name),
                    numResults = corpus.jobs.readAll().count { it.hasResult },
                    numDocs = corpus.documents.readAll().size,
                    size = corpus.size,
                    modified = System.currentTimeMillis(),
                )
            // add mutable fields
            with(corpus.metadata) {
                meta.owner = owner
                meta.name = name
                meta.eraFrom = eraFrom
                meta.eraTo = eraTo
                meta.language = language
                meta.tagset = tagset
                meta.dataset = dataset
                meta.collaborators = collaborators
                meta.viewers = viewers
                meta.sourceName = sourceName
                meta.sourceURL = sourceURL
            }
            return meta
        }
    }
}
