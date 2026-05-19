package org.ivdnt.galahad.taggers

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.File
import java.net.URI
import java.net.URL
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.app.application_profile
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.CorpusMetadata
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

data class Tagger(
    // The id should be equal to the filename
    // i.e. mytagger.yaml should have id 'mytagger'
    // This ought te be set when loading from file
    // This name will be used as hostname
    // So can only contain certain characters
    var name: String = "",
    var description: String = "",
    var period: CorpusMetadata.Period? = null,
    var annotations: List<AnnotationItem> = emptyList(),
    var attributions: List<LinkItem> = emptyList(),
    var language: String? = "", // TODO multiple languages
    // Version & URI are split from attributions because they need to be present in metadata
    var version: String = "",
    var uri: String = "",
) {
    @JsonIgnore var port: Int? = 0

    // Has to be a getter, because taggers are first initialized with an empty constructor,
    // and then filled from yaml, meaning that devport is 0 at the time of initialization
    @get:JsonIgnore
    val url: URL
        get() =
            if ("dev" in application_profile) {
                URI("http://localhost:$port").toURL()
            } else {
                URI("http://$name:8080").toURL()
            }

    //    // TODO: doubtful anyone needs this. instead grab the anotations of the current processing
    // document, as they may differ from the tagger
    //    @get:JsonIgnore
    //    val annotationSet: Set<Annotation>
    //        get() = annotations.map { it.annotation!! }.toSet()

    @get:JsonIgnore
    val principles: String
        get() = annotations.mapNotNull { it.principles }.flatten().joinToString { it.name!! }

    data class LinkItem(
        var name: String? = null,
        var details: String? = null,
        var href: String? = null,
    )

    data class AnnotationItem(
        var annotation: Annotation? = null,
        var principles: List<LinkItem>? = null,
    )

    companion object {
        private const val TAGGERS_DIR: String = "data/taggers"

        val taggers: Map<String, Tagger> =
            File(TAGGERS_DIR)
                .listFiles()
                .map {
                    Yaml(
                            Constructor(
                                Tagger::class.java,
                                LoaderOptions().apply { isEnumCaseSensitive = false },
                            )
                        )
                        .load<Tagger>(it.inputStream())
                }
                .associateBy { it.name }

        // TODO remove parameter corpus, given that each corpusLayer now has a tagger
        // TODO remove the when SOURCE_LAYER all together
        fun readOrThrow(id: String): Tagger = taggers[id] ?: throw TaggerNotFoundException(id)

        fun createSourceTagger(corpus: Corpus): Tagger {
            val metadata = corpus.metadata
            val produces =
                corpus.documents.readAll().flatMap { it.metadata.annotations.keys }.toSet()
            return Tagger(
                name = SOURCE_LAYER,
                description = "uploaded annotations",
                period = metadata.period,
                language = metadata.language,
                annotations = produces.map { AnnotationItem(it) },
            )
        }
    }
}
