package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.entities.DocumentEntities
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import java.io.File

class DocumentEvaluation(
    dir: File,
    private val corpus: Corpus,
    private val jobs: JobPair,
) : GalahadFolder(dir) {
    val entities: List<DocumentEntities.Entity> get() = entitiesCache.readOrCreate()
    private val entitiesFile = dir.resolve(ENTITIES_FILE)
    private val entitiesCache = object : ValidatedDiskValue<List<DocumentEntities.Entity>>(entitiesFile) {
        override fun isValid(lastModified: Long) = lastModified >= corpus.lastModified
        override fun set(): List<DocumentEntities.Entity> = DocumentEntities.fromLayer(corpus.jobs.readOrThrow(jobs.reference).getLayer(name))
    }
    companion object {
        const val ENTITIES_FILE = "entities.json"
    }
}