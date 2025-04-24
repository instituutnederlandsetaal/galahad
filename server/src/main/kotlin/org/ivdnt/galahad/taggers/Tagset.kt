package org.ivdnt.galahad.taggers

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.exceptions.TagsetNotFoundException
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File

class Tagset(
    // The identifier should be equal to the filename
    var identifier: String = "",
    var longName: String = "",
    var shortName: String = "",
    var punctuationTags: Set<String> = setOf()
) {
    companion object {
        val UNKNOWN: Tagset = Tagset(
            identifier = "UNKNOWN",
            longName = "Unknown Tagset",
            shortName = "UNK",
        )
        private const val TAGSETS_DIR: String = "data/tagsets"
        private val dir = File(TAGSETS_DIR)

        val tagsets: Map<String, Tagset> = dir.listFiles()
            .map { Yaml(Constructor(Tagset::class.java, LoaderOptions())).load<Tagset>(it.inputStream()) }
            .associateBy { it.identifier }

        fun readOrNull(tagger: Tagger): Tagset? = tagsets[tagger.tagset]
        fun readOrNull(id: String): Tagset? = tagsets[id]
        fun readOrThrow(id: String): Tagset = readOrNull(id) ?: throw TagsetNotFoundException(id)
    }

}