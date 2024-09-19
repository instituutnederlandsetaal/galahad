package org.ivdnt.galahad.data

import jakarta.servlet.http.HttpServletRequest
import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.app.CRUDSet
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.corpus.CorpusMetadata
import org.ivdnt.galahad.data.corpus.MutableCorpusMetadata
import org.ivdnt.galahad.exceptions.CorpusNameInvalidException
import org.ivdnt.galahad.exceptions.CorpusUnauthorizedException
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

private fun File.corpus(): Corpus {
    return Corpus(this)
}

@Service
class CorporaService(
    config: Config,
) : BaseFileSystemStore(config.getWorkingDirectory().resolve("corpora")), CRUDSet<UUID, Corpus, MutableCorpusMetadata, CorpusMetadata> {

    @Autowired
    private val request: HttpServletRequest? = null

    // The two folders in which corpora reside
    private val customDir: File = workDirectory.resolve("custom")
    private val presetsDir: File = workDirectory.resolve("presets")
    // The corpora in these folders
    val custom: List<Corpus> get() = customDir.listFiles()?.map { it.corpus() } ?: listOf()
    val presets: List<Corpus> get() = presetsDir.listFiles()?.map { it.corpus() } ?: listOf()

    // Combined sets
    val all: List<Corpus> get() = custom + presets
    // You can't just look at presets. Some datasets may reside in the customDir.
    val datasets get() = all.filter { it.metadata.expensiveGet().isDataset }

    val assaysFile get() = workDirectory.resolve("assays.cache")

    /**
     * Create a new corpus with the provided metadata. Will override the user with the request user.
     * Checks for valid corpus name and if the user is allowed to create a dataset (if isDataset is true).
     */
    override fun create(value: MutableCorpusMetadata): UUID {
        val uuid = UUID.randomUUID()
        val corpusDir = customDir.resolve(uuid.toString())
        val corpusStore = corpusDir.corpus()
        val user = User.getUserFromRequestOrThrow(request)
        val newVal = MutableCorpusMetadata(
            owner = user.id, // The creator of the request is the owner.
            name = value.name,
            eraFrom = value.eraFrom,
            eraTo = value.eraTo,
            tagset = value.tagset,
            isDataset = value.isDataset,
            collaborators = value.collaborators,
            viewers = value.viewers,
            sourceName = value.sourceName,
            sourceURL = value.sourceURL,
        )
        // Updating will check if the user is allowed to create a dataset.
        corpusStore.updateMetadata(newVal, user)

        return corpusStore.metadata.expensiveGet().uuid
    }

    override fun update(key: UUID, value: MutableCorpusMetadata): CorpusMetadata {
        val user = User.getUserFromRequestOrThrow(request)

        // Viewers are allowed to remove themselves, but no more than that.
        val original = readOrThrow(key)
        val orgMeta = original.metadata.expensiveGet()
        if (!value.isViewer(user) && orgMeta.isViewer(user)) {
            original.removeAsViewer(user)
            // A new get is needed to get the updated metadata.
            return original.metadata.expensiveGet()
        }

        // Same for collaborators
        if (!value.isCollaborator(user) && orgMeta.isCollaborator(user)) {
            original.removeAsCollaborator(user)
            // Although collaborators could change other metadata,
            // if you have chosen to remove yourself as a collaborator,
            // you probably don't want to change anything else.
            return original.metadata.expensiveGet()
        }

        val corpus = getWriteAccessOrThrow(key, request)
        val newMetadata = corpus.updateMetadata(value, user).expensiveGet()

        // Note how this is down after the updateMetadata call, because the latter performs security checks.
        // Otherwise, a viewer would be able to trigger cache invalidation.
        if (orgMeta.isDataset) {
            if (!value.isDataset) {
                // This corpus is no longer a dataset.
                // Invalidate assays.cache
                assaysFile.delete()
            }
        }
        return newMetadata
    }

    override fun delete(key: UUID): Corpus? {
        val corpus = getReadAccessOrThrow(key, request)
        // security like a pro
        val metadata: CorpusMetadata = corpus.metadata.expensiveGet()
        val user = User.getUserFromRequestOrThrow(request)
        if (!metadata.canDelete(user)) {
            throw CorpusUnauthorizedException("Cannot delete corpus $key.")
        }
        getWriteAccessOrThrow(key, request).delete()
        // Invalidate assays.cache
        assaysFile.delete()
        return null
    }

    /** Get all corpora the user can see. */
    private fun getCorporaForUser(user: User): Set<Corpus> {
        // We don't want to pollute the admin's corpora list.
        return all.filter { corpus ->
            corpus.metadata.expensiveGet().hasReadAccess(user, excludeAdmin = true)
        }.toSet()
    }

    override fun readAll(): Set<Corpus> {
        val user = User.getUserFromRequestOrThrow(request)
        return getCorporaForUser(user)
    }

    /**
     * Gives unauthorized access to the corpus given a UUID,
     * therefore it should not be directly used for external access.
     */
    fun getUncheckedCorpusAccess(corpus: UUID): Corpus {
        return getUncheckedCorpusAccessOrNull(corpus) ?: throw throw CorpusNotFoundException(corpus)
    }

    // TODO beide private maken
    fun getUncheckedCorpusAccessOrNull(corpus: UUID): Corpus? {
        val presetCorpus = presetsDir.resolve(corpus.toString())
        val customCorpus = customDir.resolve(corpus.toString())

        return if (presetCorpus.exists()) {
            presetCorpus.corpus()
        } else if (customCorpus.exists()) {
            customCorpus.corpus()
        } else {
            null
        }
    }

    // We should have the return type of this method be an object or interface of only read-methods
    // Since this is currently not the case, the 'Read' or 'Write' is just a marker of intended actions
    // but nothing is enforced, could be worth the refactor
    fun getReadAccessOrNull(corpus: UUID, request: HttpServletRequest?): Corpus? {
        if (request == null) return null
        val cs = getUncheckedCorpusAccessOrNull(corpus)
        if (cs == null) return null
        // security check
        val metadata: MutableCorpusMetadata = cs.mutableCorpusMetadata
        val user: User = User.getUserFromRequestOrThrow(request)
        if (metadata.hasReadAccess(user)) return cs
        return null
    }

    fun getWriteAccessOrNull(corpus: UUID, request: HttpServletRequest?): Corpus? {
        if (request == null) return null
        val cs = getReadAccessOrNull(corpus, request)
        // security like a pro
        val metadata: MutableCorpusMetadata? = cs?.mutableCorpusMetadata
        val user: User = User.getUserFromRequestOrThrow(request)
        if (metadata?.hasWriteAccess(user) == true) return cs
        return null
    }

    fun getReadAccessOrThrow(corpus: UUID, request: HttpServletRequest?): Corpus {
        return getReadAccessOrNull(corpus, request) ?: throw CorpusNotFoundException(corpus)
    }

    fun getWriteAccessOrThrow(corpus: UUID, request: HttpServletRequest?): Corpus {
        // Try read access first. If that fails it throws a 404.
        getReadAccessOrThrow(corpus, request)
        return getWriteAccessOrNull(corpus, request) ?: throw CorpusUnauthorizedException("No write access to corpus.")
    }

    override fun readOrNull(key: UUID) = getReadAccessOrNull(key, request)

    override fun readOrThrow(key: UUID) = getReadAccessOrThrow(key, request)
}