package org.ivdnt.galahad.web.service

import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpora
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.CorpusMetadata
import org.ivdnt.galahad.corpora.MutableCorpusMetadata
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.CorpusUnauthorizedException
import org.ivdnt.galahad.files.GalahadFolder
import org.springframework.stereotype.Service
import java.util.*

@Service
class CorporaService(
    config: Config,
) : GalahadFolder(config.getWorkingDirectory().resolve("corpora")) {

    val custom = Corpora(dir.resolve("custom"))
    val presets = Corpora(dir.resolve("presets"))

    val all: List<Corpus> get() = custom.readAll().toList() + presets.readAll().toList()
    val datasets get() = all.filter { it.mutableMetadata.dataset }
    val assaysFile get() = dir.resolve("benchmarks.json")

    fun readAll(user: User): Set<CorpusMetadata> {
        return all.map { it.immutableMetadata }.filter { it.hasReadAccess(user, excludeAdmin = true) }.toSet()
    }

    fun readAllDatasets(): Set<CorpusMetadata> {
        return datasets.map { it.immutableMetadata }.toSet()
    }

    fun readAsReaderOrThrow(key: UUID, user: User): Corpus {
        val (corpus, _) = findOrThrow(key)
        return corpus.also { if (!it.mutableMetadata.hasReadAccess(user)) throw CorpusUnauthorizedException("No read access to corpus.") }
    }

    fun readAsWriterOrThrow(key: UUID, user: User): Corpus {
        val (corpus, _) = findOrThrow(key)
        return corpus.also { if (!it.mutableMetadata.hasWriteAccess(user)) throw CorpusUnauthorizedException("No write access to corpus.") }
    }

    fun delete(key: UUID, user: User) {
        val (corpus, corpora) = findOrThrow(key)

        if (corpus.mutableMetadata.canDelete(user)) {
            corpora.deleteOrThrow(key.toString())
        } else {
            throw CorpusUnauthorizedException("No delete access to corpus.")
        }
    }

    private fun findOrThrow(uuid: UUID): Pair<Corpus, Corpora> {
        val key = uuid.toString()
        // We don't want to access CorporaManager.all here, because it is expensive.
        val customCorpus = custom.readOrNull(key)
        val presetCorpus = presets.readOrNull(key)

        val corpus = customCorpus ?: presetCorpus ?: throw CorpusNotFoundException(key)
        val corpora =
            customCorpus?.let { custom } ?: presetCorpus?.let { presets } ?: throw CorpusNotFoundException(key)
        return Pair(corpus, corpora)
    }

    internal fun readCorpusUnsafe(key: UUID): Corpus {
        return findOrThrow(key).first
    }

    fun createOrThrow(value: MutableCorpusMetadata, user: User): CorpusMetadata {
        // new corpora are always custom
        value.user = user
        value.id = UUID.randomUUID()
        return custom.createOrThrow(value).immutableMetadata
    }

    fun update(key: UUID, value: MutableCorpusMetadata, user: User): CorpusMetadata {
        val (_, corpora) = findOrThrow(key)
        value.user = user
        value.id = key
        return corpora.updateOrThrow(value).immutableMetadata
    }
}