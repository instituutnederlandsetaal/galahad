package org.ivdnt.galahad.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.cfg.DatatypeFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpora
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.MutableCorpusMetadata
import org.ivdnt.galahad.formats.InternalFile
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File
import java.net.URL
import java.util.*
import kotlin.io.path.createTempDirectory

object TestUtil {
    var corpus: Corpus = createCorpus()

    fun get(path: String): File = File(this::class.java.classLoader.getResource(path)!!.toURI())

    val mapper: ObjectMapper = JsonMapper.builder()
        .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .build()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    fun createCorpus(workdir: File? = null, isDataset: Boolean = false, isAdmin: Boolean = false): Corpus {
        val parent = workdir ?: createTempDirectory().toFile()
        val corpora = Corpora(parent)
        val meta = MutableCorpusMetadata(
            "you",
            "testCorpus",
            0,
            0,
            "",
            "tagset",
            isDataset,
            mutableSetOf("collaborator1", "collaborator2"),
            mutableSetOf(),
            "source name",
            URL("http://source.url")
        )
        meta.user = User("testUser", isAdmin)
        meta.id = UUID.randomUUID()
        return corpora.createOrThrow(meta)
    }

    fun assertPlainText(folder: String, file: InternalFile) {
        // Plain text
        val plaintext = get("$folder/plaintext.txt").readText()
        assertEquals(plaintext, file.plaintext)
    }

    fun assertPlaintextAndSourcelayer(folder: String, file: InternalFile) {
        // Plain text
        assertPlainText(folder, file)
        // Source layer
        val jsonExpected = get("$folder/layer.json").readText()
        val json = mapper.writeValueAsString(file.layer)
        assertEquals(jsonExpected, json)
    }
}