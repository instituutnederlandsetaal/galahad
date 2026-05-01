package org.ivdnt.galahad.util

import java.io.File
import java.net.URL
import java.util.*
import kotlin.io.path.createTempDirectory
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpora
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.CorpusMetadata
import org.springframework.http.HttpHeaders

object TestUtil {
    const val TAGGER_NAME: String = "pie-tdn-all"
    const val TAGSET_NAME: String = "TDN-Core"
    const val TEST_USER: String = "testUser"

    fun get(path: String): File = File(this::class.java.classLoader.getResource(path)!!.toURI())

    fun createFilledCorpus(config: Config, dataset: Boolean = false): Corpus {
        val corpus = createCorpus(config, dataset)
        val files = get("formats/shared/converter").listFiles()
        files.forEach { corpus.documents.createOrThrow(it) }
        return corpus
    }

    fun createCorpus(config: Config? = null, dataset: Boolean = false): Corpus {
        val parent =
            config?.getWorkingDirectory()?.resolve("corpora")?.resolve("user")
                ?: createTempDirectory().toFile()
        val corpora = Corpora(parent)
        val user = if (dataset) User("admin") else User(TEST_USER)
        val meta =
            CorpusMetadata(
                "testCorpus",
                user.id,
                dataset,
                CorpusMetadata.Period(1200, 1300),
                "Dutch",
                "TDN-Core",
                CorpusMetadata.Source("source name", URL("http://source.url")),
                mutableSetOf("collaborator"),
                mutableSetOf("viewer"),
            )
        meta.user = user
        meta.id = UUID.randomUUID()
        return corpora.createOrThrow(meta)
    }

    fun assignHeaders(headers: HttpHeaders, user: String = TEST_USER) {
        headers.set(User.USER_HEADER, user)
    }
}
