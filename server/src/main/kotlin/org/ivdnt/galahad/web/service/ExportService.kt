package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.documents.Document
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.MergeNotImplementedException
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
    ) {
        val corpusExport = getCorpusExport(corpus, job, format, posHeadOnly)
        corpusExport.corpus.export(corpusExport, formatMapper = {
            try {
                // Document conversions.
                val docExport = DocumentExport.create(corpusExport, it)
                return@export if (shouldMerge && mergeFormatMatches(it, format)) {
                    logger.info("Merging ${it.name} of format ${it.metadata.format}")
                    docExport.merge()
                } else {
                    logger.info("Converting ${it.name} of format ${it.metadata.format} to $format")
                    docExport.convert()
                }
            } catch (e: MergeNotImplementedException) {
                throw e
            } catch (e: Exception) {
                throw Exception("Could not convert file ${it.name} to format ${format}. ${e.message}.")
            }
        }, filter = {
            // Filter out untagged documents.
                DocumentExport.create(corpusExport, it).layer != Layer.EMPTY
        }, outputStream = response?.outputStream)
    }

    private fun getCorpusExport(
        corpusID: UUID,
        jobName: String,
        format: DocumentFormat,
        posHeadOnly: Boolean
    ): CorpusExport {
        val corpus = corpora.readAsWriterOrThrow(corpusID, user)
        return CorpusExport.create(corpus, jobName, format, posHeadOnly, user)
    }

    private fun getDocumentExport(
        corpus: UUID,
        job: String,
        document: String,
        format: DocumentFormat,
        posHeadOnly: Boolean
    ): DocumentExport = DocumentExport.create(getCorpusExport(corpus, job, format, posHeadOnly), document)

    private fun mergeFormatMatches(
        it: Document, format: DocumentFormat,
    ): Boolean {
        var otherFormat = it.metadata.format
        // Overwrite the format for legacy formats that can in fact be merged.
        if (otherFormat == DocumentFormat.TeiP5Legacy) {
            otherFormat = DocumentFormat.TeiP5
        }
        return otherFormat == format
    }

    fun getCorpusName(corpus: UUID): String = corpora.readAsWriterOrThrow(corpus, user).mutableMetadata.name
}