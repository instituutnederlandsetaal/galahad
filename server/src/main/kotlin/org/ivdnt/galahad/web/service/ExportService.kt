package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.export.CorpusExport
import org.ivdnt.galahad.export.DocumentExport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
class ExportService(val corpora: CorporaService) : Logging {
    @Autowired
    private val request: HttpServletRequest? = null

    @Autowired
    private val response: HttpServletResponse? = null

    private val user get() = User.fromRequest(request)

    fun mergeDoc(corpus: UUID, job: String, document: String, posHeadOnly: Boolean): File {
        val doc = corpora.readAsWriterOrThrow(corpus, user).documents.readOrThrow(document)
        val export = getDocumentExport(corpus, job, document, doc.metadata.format, posHeadOnly)
        return export.merge()
    }

    fun convertDoc(corpus: UUID, job: String, document: String, format: DocumentFormat, posHeadOnly: Boolean): File {
        val export = getDocumentExport(corpus, job, document, format, posHeadOnly)
        return export.convert()
    }

    /**
     * Export corpus job in a stream. Allows for longer response times, because converting takes time.
     */
    fun exportCorpusJobInFormat(
        corpus: UUID,
        job: String,
        format: DocumentFormat,
        shouldMerge: Boolean,
        posHeadOnly: Boolean,
    ): Unit = getCorpusExport(corpus, job, format, posHeadOnly).export(response!!.outputStream)

    private fun getCorpusExport(
        corpusID: UUID, jobName: String, format: DocumentFormat, posHeadOnly: Boolean, shouldMerge: Boolean = false
    ): CorpusExport {
        val corpus = corpora.readAsWriterOrThrow(corpusID, user)
        return CorpusExport.create(corpus, jobName, format, posHeadOnly, user, shouldMerge)
    }

    private fun getDocumentExport(
        corpus: UUID, job: String, document: String, format: DocumentFormat, posHeadOnly: Boolean
    ): DocumentExport = DocumentExport.create(getCorpusExport(corpus, job, format, posHeadOnly), document)

    fun getCorpusName(corpus: UUID): String = corpora.readAsWriterOrThrow(corpus, user).mutableMetadata.name
}