package org.ivdnt.galahad.export

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.documents.Document
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.corpora.jobs.Job
import org.ivdnt.galahad.taggers.Tagger
import java.io.OutputStream

class DocumentExport private constructor(
    val corpus: Corpus,
    private val job: Job,
    val document: Document,
    val user: User,
    val format: DocumentFormat,
    val tagger: Tagger,
    private val posHeadOnly: Boolean,
) {
    val layer: Layer = job.getLayer(document)
    val sourceLayer: Layer by lazy { corpus.jobs.readOrNull(SOURCE_LAYER_NAME)?.getLayer(document) ?: Layer.EMPTY }

    fun convert(out: OutputStream): Unit = LayerConverter.create(this).convert(out)
    fun merge(out: OutputStream): Unit = LayerMerger.create(this).merge(out)
    fun cmdi(out: OutputStream): Unit = CmdiMetadata(this).write(out)

    companion object {
        fun create(export: CorpusExport, doc: Document): DocumentExport = create(export, doc.name)

        fun create(export: CorpusExport, docName: String): DocumentExport = DocumentExport(
            corpus = export.corpus,
            job = export.job,
            document = export.corpus.documents.readOrThrow(docName),
            user = export.user,
            format = export.format,
            posHeadOnly = export.posHeadOnly,
            tagger = export.tagger
        )
    }
}
