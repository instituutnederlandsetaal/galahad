package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.export.CorpusExport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExportService(private val corpora: CorporaService) : Logging {
    @Autowired private val request: HttpServletRequest? = null

    @Autowired private val response: HttpServletResponse? = null

    private val user
        get() = User.fromRequest(request)

    /** Export corpus in a stream for longer response times, because converting takes time. */
    fun convertOrMergeCorpus(
        corpus: UUID,
        job: String,
        format: DocumentFormat,
        merge: Boolean,
        posHead: Boolean,
    ): Unit =
        CorpusExport(corpora.readOrThrow(corpus, user), job, format, user, merge, posHead)
            .export(response!!.outputStream)

    fun convertDocument(
        corpus: UUID,
        layer: String,
        document: String,
        format: DocumentFormat,
        posHead: Boolean,
    ): Unit =
        corpora
            .readOrThrow(corpus, user)
            .let { corpus -> CorpusExport(corpus, layer, format, user, false, posHead) }
            .document(document)
            .convert(response!!.outputStream)

    fun mergeDocument(corpus: UUID, layer: String, document: String, posHead: Boolean) {
        val doc = corpora.readOrThrow(corpus, user).documents.readOrThrow(document)
        corpora
            .readOrThrow(corpus, user)
            .let { corpus -> CorpusExport(corpus, layer, doc.metadata.format, user, true, posHead) }
            .document(document)
            .merge(response!!.outputStream)
    }

    fun getCorpusName(corpus: UUID): String = corpora.readOrThrow(corpus, user).metadata.name
}
