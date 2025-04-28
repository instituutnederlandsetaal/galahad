package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.export.CorpusExport
import org.ivdnt.galahad.export.DocumentExport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class ExportService(val corpora: CorporaService) : Logging {
    @Autowired
    private val request: HttpServletRequest? = null

    @Autowired
    private val response: HttpServletResponse? = null

    private val user get() = User.fromRequest(request)

    fun mergeDoc(corpus: UUID, job: String, document: String, posHeadOnly: Boolean) {
        val doc = corpora.readAsWriterOrThrow(corpus, user).documents.readOrThrow(document)
        val export = getDocumentExport(corpus, job, document, doc.metadata.format, posHeadOnly, true)
        export.merge(response!!.outputStream)
    }

    fun convertDoc(corpus: UUID, job: String, document: String, format: DocumentFormat, posHeadOnly: Boolean): Unit =
        getDocumentExport(corpus, job, document, format, posHeadOnly, false).convert(response!!.outputStream)

    /**
     * Export corpus job in a stream. Allows for longer response times, because converting takes time.
     */
    fun exportCorpusJobInFormat(
        corpus: UUID,
        job: String,
        format: DocumentFormat,
        shouldMerge: Boolean,
        posHeadOnly: Boolean,
    ): Unit = getCorpusExport(corpus, job, format, posHeadOnly, shouldMerge).export(response!!.outputStream)

    private fun getCorpusExport(
        corpusID: UUID, jobName: String, format: DocumentFormat, posHeadOnly: Boolean, shouldMerge: Boolean
    ): CorpusExport {
        val corpus = corpora.readAsWriterOrThrow(corpusID, user)
        return CorpusExport.create(corpus, jobName, format, user, shouldMerge, posHeadOnly)
    }

    private fun getDocumentExport(
        corpus: UUID, job: String, document: String, format: DocumentFormat, posHeadOnly: Boolean, shouldMerge: Boolean
    ): DocumentExport = DocumentExport.create(getCorpusExport(corpus, job, format, posHeadOnly, shouldMerge), document)

    fun getCorpusName(corpus: UUID): String = corpora.readAsWriterOrThrow(corpus, user).mutableMetadata.name
}