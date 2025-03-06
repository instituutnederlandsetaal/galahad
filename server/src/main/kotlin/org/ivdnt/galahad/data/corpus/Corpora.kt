package org.ivdnt.galahad.data.corpus

import org.apache.logging.log4j.kotlin.logger
import org.ivdnt.galahad.filesystem.GalahadFile
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import java.io.File
import java.util.*

class Corpora(
    dir: File,
) : GalahadFile(dir) {
    /**
     * Create a new corpus with the provided metadata. Will override the user with the request user.
     * Checks for valid corpus name and if the user is allowed to create a dataset (if isDataset is true).
     */
    fun createOrThrow(user: User, value: MutableCorpusMetadata): Corpus {
        val uuid = UUID.randomUUID()
        return Corpus.create(user, dir.resolve(uuid.toString()), value)
    }
    fun readAll(): Set<Corpus> = dir.listFiles()?.map { Corpus(it) }?.toSet() ?: setOf()

    fun readOrNull(key: UUID): Corpus? =
        if (dir.resolve(key.toString()).exists()) Corpus(dir.resolve(key.toString())) else null

    fun readOrThrow(key: UUID) = readOrNull(key) ?: throw CorpusNotFoundException(key)

    fun deleteOrThrow(key: UUID) {
        readOrThrow(key) // does it exist?
        if (!dir.resolve(key.toString()).deleteRecursively()) {
            logger.warn("Partial deletion of $key")
        }
    }

    //////////////////////////////

    fun update(key: UUID, newMeta: MutableCorpusMetadata, user: User): CorpusMetadata {
        val corpus = readOrThrow(key)
        val oldMeta = corpus.mutableMetadata
        val cleanMetadata = MutableCorpusMetadata.clean(user, newMeta, oldMeta)
        corpus.mutableMetadata = cleanMetadata
        return corpus.immutableMetadata
    }
}