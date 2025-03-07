package org.ivdnt.galahad.tagset

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.exceptions.TagsetNotFoundException
import org.ivdnt.galahad.filesystem.GalahadFolder
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File

const val TAGGERS_DIR = "data/tagsets"

class TagsetStore : GalahadFolder(
    File(TAGGERS_DIR)
), Logging {

    val tagsets: Set<Tagset>
        get() = dir.listFiles()
            ?.map { getTagsetFromFile(it) }
            ?.toSet()
            ?: setOf()

    private fun getTagsetFromFile(file: File): Tagset {
        val tagset = Yaml(Constructor(Tagset::class.java, LoaderOptions())).load<Tagset>(file.inputStream())
        tagset.identifier = file.canonicalFile.nameWithoutExtension // the tagset name is set here
        return tagset
    }

    fun getOrThrow(identifier: String): Tagset {
        return getOrNull(identifier) ?: throw TagsetNotFoundException(identifier)
    }

    fun getOrNull(identifier: String?): Tagset? {
        val tagsetFile = dir.resolve("$identifier.yaml")
        return if (tagsetFile.exists()) {
            getTagsetFromFile(tagsetFile)
        } else {
            null
        }
    }

}