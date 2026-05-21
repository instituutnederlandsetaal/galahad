package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import java.util.*
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpora
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.CorpusMetadata
import org.ivdnt.galahad.corpora.CorpusStatistics
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.UserUnauthorizedException
import org.ivdnt.galahad.files.GalahadFolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CorporaService(@Autowired config: Config) :
    GalahadFolder(config.getWorkingDirectory().resolve("corpora")) {

    @Autowired private val request: HttpServletRequest? = null

    private val user
        get() = User.fromRequest(request)

    private val custom: Corpora = Corpora(dir.resolve("user"))
    private val presets: Corpora = Corpora(dir.resolve("datasets"))

    private val all: List<Corpus>
        get() = custom.readAll() + presets.readAll()

    fun readAll(): List<CorpusStatistics> =
        all.map { it.statistics }.filter { it.canRead(user, excludeAdmin = true) }

    fun readOrThrow(key: UUID): Corpus {
        val (corpus, _) = findOrThrow(key)
        return corpus.also {
            if (!it.metadata.canRead(user)) throw UserUnauthorizedException("Cannot read corpus.")
        }
    }

    fun writeOrThrow(key: UUID): Corpus {
        val (corpus, _) = findOrThrow(key)
        return corpus.also {
            if (!it.metadata.canWrite(user)) throw UserUnauthorizedException("Cannot edit corpus.")
        }
    }

    fun createOrThrow(value: CorpusMetadata): CorpusStatistics {
        // new corpora are always custom
        value.user = user
        value.id = UUID.randomUUID()
        return custom.createOrThrow(value).statistics
    }

    fun updateOrThrow(key: UUID, value: CorpusMetadata): CorpusStatistics {
        val (corpus, corpora) = findOrThrow(key)
        // Read access, because reader should be able to remove themselves
        if (corpus.metadata.canRead(user)) {
            value.user = user
            value.id = key
            return corpora.updateOrThrow(value).statistics
        }
        throw UserUnauthorizedException("Cannot edit corpus.")
    }

    fun deleteOrThrow(key: UUID) {
        val (corpus, corpora) = findOrThrow(key)
        if (corpus.metadata.canDelete(user)) {
            corpora.deleteOrThrow(key.toString())
        } else {
            throw UserUnauthorizedException("Cannot delete corpus.")
        }
    }

    private fun findOrThrow(uuid: UUID): Pair<Corpus, Corpora> {
        val key = uuid.toString()
        // We don't want to access CorporaManager.all here, because it is expensive.
        val customCorpus = custom.readOrNull(key)
        val presetCorpus = presets.readOrNull(key)

        val corpus = customCorpus ?: presetCorpus ?: throw CorpusNotFoundException(key)
        val corpora =
            customCorpus?.let { custom }
                ?: presetCorpus?.let { presets }
                ?: throw CorpusNotFoundException(key)
        return Pair(corpus, corpora)
    }
}
