package org.ivdnt.galahad.taggers

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.app.application_profile
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.net.URI
import java.net.URL

class Tagger(
    // The id should be equal to the filename
    // i.e. mytagger.yaml should have id 'mytagger'
    // This ought te be set when loading from file
    // This name will be used as hostname
    // So can only contain certain characters
    var id: String = "",
    var description: String = "",
    var tagset: String? = null,
    var eraFrom: Int = 0,
    var eraTo: Int = 0,
    var annotations: Set<Annotation> = emptySet(),
    var model: LinkItem = LinkItem(),
    var software: LinkItem = LinkItem(),
    var dataset: LinkItem = LinkItem(),
    var trainedBy: String = "",
    var date: String = "",
    var language: String? = "",
    var version: String = "",
) {
    @JsonIgnore
    var devport: Int? = 0

    // Has to be a getter, because taggers are first initialized with an empty constructor,
    // and then filled from yaml, meaning that devport is 0 at the time of initialization
    @get:JsonIgnore
    val url: URL
        get() = if ("dev" in application_profile) {
            URI("http://localhost:$devport").toURL()
        } else {
            URI("http://$id:8080").toURL()
        }

    class LinkItem(
        var name: String = "",
        var href: String = "",
    )

    companion object {
        private const val TAGGERS_DIR: String = "data/taggers"
        private val dir: File = File(TAGGERS_DIR)

        val taggers: Map<String, Tagger> = dir.listFiles()
            .map {
                Yaml(
                    Constructor(
                        Tagger::class.java,
                        LoaderOptions().apply { isEnumCaseSensitive = false })
                ).load<Tagger>(it.inputStream())
            }
            .associateBy { it.id }

        fun readOrThrow(id: String, corpus: Corpus? = null): Tagger = when (id) {
            SOURCE_LAYER_NAME -> corpus?.jobs?.readOrThrow(SOURCE_LAYER_NAME)?.metadata?.tagger
                ?: throw TaggerNotFoundException(id)
            else -> taggers[id] ?: throw TaggerNotFoundException(id)
        }

        fun createSourceTagger(corpus: Corpus): Tagger {
            val metadata = corpus.immutableMetadata
            val produces = corpus.documents.readAll().flatMap { it.metadata.annotations }.toSet()
            return Tagger(
                id = SOURCE_LAYER_NAME,
                description = "uploaded annotations",
                tagset = metadata.tagset,
                eraFrom = metadata.eraFrom,
                eraTo = metadata.eraTo,
                language = metadata.language,
                annotations = produces,
            )
        }
    }
}