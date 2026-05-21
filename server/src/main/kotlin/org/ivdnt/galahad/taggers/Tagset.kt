package org.ivdnt.galahad.taggers

import java.io.File
import org.ivdnt.galahad.exceptions.TagsetNotFoundException
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

class Tagset(
    // The identifier should be equal to the filename
    var name: String = "",
    var description: String = "",
    var punctuation: Array<String> = emptyArray(),
) {
    companion object {
        val UNKNOWN: Tagset = Tagset(name = "UNKNOWN", description = "Unknown Tagset")
        private const val TAGSETS_DIR: String = "data/tagsets"

        val tagsets: Map<String, Tagset> =
            File(TAGSETS_DIR)
                .listFiles()
                .map {
                    Yaml(Constructor(Tagset::class.java, LoaderOptions()))
                        .load<Tagset>(it.inputStream())
                }
                .associateBy { it.name }

        fun readOrNull(name: String?): Tagset? = tagsets[name]

        fun readOrThrow(name: String): Tagset =
            readOrNull(name) ?: throw TagsetNotFoundException(name)
    }
}
