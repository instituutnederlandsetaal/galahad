package org.ivdnt.galahad.taggers

import com.fasterxml.jackson.annotation.JsonIgnore
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
    var period: Period? = null,
    var annotations: List<AnnotationItem> = emptyList(),
    var attributions: List<LinkItem> = emptyList(),
    var language: String? = "", // TODO multiple languages
    // Version & URI are split from attributions because they need to be present in metadata
    var version: String = "",
    var uri: String = "",
) {
    @JsonIgnore
    var port: Int? = 0

    // Has to be a getter, because taggers are first initialized with an empty constructor,
    // and then filled from yaml, meaning that devport is 0 at the time of initialization
    @get:JsonIgnore
    val url: URL
        get() = if ("dev" in application_profile) {
            URI("http://localhost:$port").toURL()
        } else {
            URI("http://$id:8080").toURL()
        }

    @get:JsonIgnore
    val annotationSet: Set<Annotation>
        get() = annotations.map { it.annotation!! }.toSet()

    @get:JsonIgnore
    val principles: String
        get() = annotations.mapNotNull { it.principles }.flatten().joinToString { it.name!! }

    class LinkItem(
        var name: String? = null,
        var details: String? = null,
        var href: String? = null,
    )

    class AnnotationItem(
        var annotation: Annotation? = null,
        var principles: List<LinkItem>? = null,
    )

    class Period(
        var from: Int = 0,
        var to: Int = 0,
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
                period = Period(metadata.eraFrom, metadata.eraTo),
                language = metadata.language,
                annotations = produces.map { AnnotationItem(it) },
            )
        }
    }
}