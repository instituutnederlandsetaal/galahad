package org.ivdnt.galahad.util

import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpora
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.MutableCorpusMetadata
import org.ivdnt.galahad.documents.Document
import org.springframework.http.HttpHeaders
import java.io.File
import java.net.URL
import java.util.*
import kotlin.io.path.createTempDirectory

object TestUtil {
    private var dummyCorpus: Corpus = createCorpus()

    fun get(path: String): File = File(this::class.java.classLoader.getResource(path)!!.toURI())

    fun getDoc(path: String): Document {
        val file = get(path)
        // Might already exist due to another unit test, so try to read it first.
        return dummyCorpus.documents.readOrNull(file.name) ?: dummyCorpus.documents.createOrThrow(file)
    }

//    fun getLayer(doc: Document, job: String = SOURCE_LAYER_NAME): Layer =
//        corpus.jobs.readOrThrow(job).getLayer(doc.name)

    fun createEmptyCorpus(config: Config): Corpus {
        val workdir = config.getWorkingDirectory().resolve("corpora").resolve("user")
        return createCorpus(workdir)
    }

    fun createFilledCorpus(config: Config): Corpus {
        val corpus = createEmptyCorpus(config)
        val files = get("formats/shared/converter").listFiles()
        files.forEach { corpus.documents.createOrThrow(it) }
        return corpus
    }

    fun createCorpus(workdir: File? = null, isDataset: Boolean = false, isAdmin: Boolean = false): Corpus {
        val parent = workdir ?: createTempDirectory().toFile()
        val corpora = Corpora(parent)
        val meta = MutableCorpusMetadata(
            "testCorpus",
            TestConfig.TEST_USER,
            1200,
            1300,
            "Dutch",
            "TDN-Core",
            isDataset,
            mutableSetOf("collaborator"),
            mutableSetOf("viewer"),
            "source name",
            URL("http://source.url")
        )
        meta.user = User("testUser", isAdmin)
        meta.id = UUID.randomUUID()
        return corpora.createOrThrow(meta)
    }

    fun assignHeaders(headers: HttpHeaders, user: String = TestConfig.TEST_USER) {
        headers.set(User.USER_HEADER, user)
    }
}
