package org.ivdnt.galahad.data.corpus

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.app.JSONable
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
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
    @JsonProperty("sourceAnnotationTypes") val sourceAnnotationTypes: Set<String> = setOf(),
    @JsonProperty("uuid") val uuid: UUID = UUID(0, 0),
    @JsonProperty("activeJobs") val activeJobs: Int = 0,
    @JsonProperty("numResults") val numResults: Int = 0,
    @JsonProperty("numDocs") val numDocs: Int = 0,
    @JsonProperty("sizeInBytes") val sizeInBytes: Long = 0,
    @JsonProperty("lastModified") val lastModified: Long = 0,
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
), JSONable {
    companion object {
        fun create(corpus: Corpus): CorpusMetadata {
            val allJobs = corpus.jobs.readAll()
            val allDocs = corpus.documents.readAll()
            // TODO: no need to recalculate this every time, when the reason we're recalculating the metadata is because a job has changed
            val uniqueAnnotations = allDocs.flatMap { it.metadata.annotationTypes }.toSet()

            val meta = CorpusMetadata(
                // Immutable/calculated fields
                sourceAnnotationTypes = uniqueAnnotations,
                uuid = UUID.fromString(corpus.name),
                activeJobs = allJobs.count { it.isActive == true },
                numResults = allJobs.count { it.hasResult },
                numDocs = allDocs.size,
                sizeInBytes = corpus.sizeInBytes, // expensive
                lastModified = System.currentTimeMillis(),
            )
            // add mutable fields
            with (corpus.mutableMetadata) {
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