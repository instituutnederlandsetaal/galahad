package org.ivdnt.galahad.export

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.documents.Document
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.corpora.jobs.Job
import org.ivdnt.galahad.taggers.Tagger
import java.io.File
import kotlin.io.path.createTempDirectory

class DocumentExport private constructor(
    val corpus: Corpus,
    private val job: Job,
    val document: Document,
    val user: User,
    val format: DocumentFormat,
    private val posHeadOnly: Boolean,
) {
    val layer: Layer = job.layer(document)
    val sourceLayer: Layer by lazy { corpus.jobs.readOrNull(SOURCE_LAYER_NAME)?.layer(document) ?: Layer.EMPTY }
    val tagger: Tagger = Tagger.readOrThrow(job.name, corpus)
    private val fileName: String =
        "$document.uploadedFile.nameWithoutExtension}.${format.extension}" // TODO this will double .tei.tei.xml
    private val file: File = createTempDirectory().resolve(fileName).toFile()

    fun convert(): File = file.also { LayerConverter.create(this).convert(file.outputStream()) }
    fun merge(): File = file.also { LayerMerger.create(this).merge(file.outputStream()) }

    companion object {
        fun create(export: CorpusExport, doc: Document): DocumentExport = create(export, doc.name)

        fun create(export: CorpusExport, docName: String): DocumentExport = DocumentExport(
            corpus = export.corpus,
            job = export.job,
            document = export.corpus.documents.readOrThrow(docName),
            user = export.user,
            format = export.targetFormat,
            posHeadOnly = export.posHeadOnly,
        )
    }
}
