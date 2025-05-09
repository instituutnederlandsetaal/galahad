package org.ivdnt.galahad.corpora

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL
import java.util.*

/**
 * Metadata about a corpus, to be stored in a cache file,
 * as its immutable fields can become invalidated (e.g. [numDocs], [lastModified]),
 * and of course any [MutableCorpusMetadata] field.
 * This is the superset of [MutableCorpusMetadata], and contains,
 * in addition to the mutable fields of the latter, any immutable fields like [sizeInBytes].
 * [sizeInBytes] is expensive to calculate, hence the cache file.
 */
class CorpusMetadata(
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
    val sizeInBytes: Long = 0,
    val lastModified: Long = 0,
) : MutableCorpusMetadata(
    owner = owner,
    name = name,
    eraFrom = eraFrom,
    eraTo = eraTo,
    language = language,
    tagset = tagset,
    dataset = dataset,
    collaborators = collaborators.toMutableSet(),
    viewers = viewers.toMutableSet(),
    sourceName = sourceName,
    sourceURL = sourceURL
) {
    companion object {
        fun create(corpus: Corpus): CorpusMetadata {
            val meta = CorpusMetadata(
                // Immutable/calculated fields
                // sourceAnnotationTypes = uniqueAnnotations,
                uuid = UUID.fromString(corpus.name),
                numResults = corpus.jobs.readAll().count { it.hasResult },
                numDocs = corpus.documents.readAll().size,
                sizeInBytes = corpus.sizeInBytes,
                lastModified = System.currentTimeMillis(),
            )
            // add mutable fields
            with(corpus.mutableMetadata) {
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