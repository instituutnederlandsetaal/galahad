package org.ivdnt.galahad.taggers

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.app.JSONable
import org.ivdnt.galahad.app.application_profile
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.annotations.AnnotationType
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.net.URI
import java.net.URL

const val TAGGERS_DIR = "data/taggers"

class Tagger(
    // The id should be equal to the filename
    // i.e. mytagger.yaml should have id 'mytagger'
    // This ought te be set when loading from file
    // This name will be used as hostname
    // So can only contain certain characters
    @JsonProperty("id") var id: String = "",
    @JsonProperty("description") var description: String = "",
    @JsonProperty("tagset") var tagset: String? = null,
    @JsonProperty("eraFrom") var eraFrom: Int = 0,
    @JsonProperty("eraTo") var eraTo: Int = 0,
    @JsonProperty("produces") var produces: Set<String> = setOf(),
    @JsonProperty("model") var model: LinkItem = LinkItem(),
    @JsonProperty("software") var software: LinkItem = LinkItem(),
    @JsonProperty("dataset") var dataset: LinkItem = LinkItem(),
    @JsonProperty("trainedBy") var trainedBy: String = "",
    @JsonProperty("date") var date: String = "",
    @JsonProperty("language") var language: String? = "",
) : JSONable {
    @JsonIgnore
    var version: String = ""

    @JsonIgnore
    var devport: Int? = 0

    // Has to be a getter, because taggers are first initialized with an empty constructor,
    // and then filled from yaml, meaning that devport is 0 at the time of initialization
    @get:JsonIgnore
    val url: URL
        get() = if (application_profile.contains("dev")) {
            URI("http://localhost:$devport").toURL()
        } else {
            URI("http://$id:8080").toURL()
        }

    // Also has to be a getter
    @get:JsonIgnore
    val annotationTypes: List<AnnotationType>
        get() = produces.map { AnnotationType.fromString(it) }

    class LinkItem(
        @JsonProperty("name") var name: String = "",
        @JsonProperty("href") var href: String = "",
    )

    companion object {
        val EMPTY: Tagger = Tagger("EMPTY")

        val dir = File(TAGGERS_DIR)
        val taggers: Map<String, Tagger> by lazy {
            dir.listFiles()
                .map { Yaml(Constructor(Tagger::class.java, LoaderOptions())).load<Tagger>(it.inputStream()) }
                .associateBy { it.id }
        }

        fun readOrThrow(id: String, corpus: Corpus? = null): Tagger = when (id) {
            SOURCE_LAYER_NAME -> corpus?.jobs?.readOrThrow(SOURCE_LAYER_NAME)?.metadata?.tagger
                ?: throw TaggerNotFoundException(id)

            EMPTY.id -> EMPTY
            else -> taggers[id] ?: throw TaggerNotFoundException(id)
        }

        fun createSourceTagger(corpus: Corpus): Tagger {
            val metadata = corpus.immutableMetadata
            val produces = corpus.documents.readAll().flatMap { it.metadata.annotationTypes }.toSet()
            return Tagger(
                id = SOURCE_LAYER_NAME,
                description = "uploaded annotations",
                tagset = metadata.tagset,
                eraFrom = metadata.eraFrom,
                eraTo = metadata.eraTo,
                language = metadata.language,
                produces = produces,
            )
        }
    }
}