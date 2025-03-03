package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.exceptions.MergeNotImplementedException
import org.ivdnt.galahad.formats.CorpusTransformMetadata
import org.ivdnt.galahad.formats.DocumentTransformMetadata
import org.ivdnt.galahad.formats.InternalFile
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


    fun mergeDoc(corpus: UUID, job: String, document: String, posHeadOnly: Boolean): InternalFile {
        val doc = corpora.getWriteAccessOrThrow(corpus, request).documents.readOrThrow(document)
        val dtm = getDocumentTransformMetadata(corpus, job, document, doc.format)
        return mergeDoc(dtm, posHeadOnly)
    }

    fun mergeDoc(dtm: DocumentTransformMetadata, posHeadOnly: Boolean): InternalFile {
        if (posHeadOnly) {
            dtm.convertLayerToPosHead()
        }
        return dtm.document.merge(dtm)
    }

    fun convertDoc(corpus: UUID, job: String, document: String, format: String, posHeadOnly: Boolean): File {
        val dtm = getDocumentTransformMetadata(corpus, job, document, DocumentFormat.fromString(format))
        return convertDoc(dtm, posHeadOnly)
    }

    fun convertDoc(dtm: DocumentTransformMetadata, posHeadOnly: Boolean): File {
        if (posHeadOnly) {
            dtm.convertLayerToPosHead()
        }
        return dtm.document.convert(dtm)
    }

    /**
     * Export corpus job in a stream. Allows for longer response times, because converting takes time.
     */
    fun exportCorpusJobInFormat(
        corpus: UUID,
        job: String,
        formatName: String,
        shouldMerge: Boolean,
        posHeadOnly: Boolean,
    ) {
        val format = DocumentFormat.fromString(formatName)
        val ctm = getCorpusTransformMetadata(corpus, job, format)
        ctm.corpus.getZipped(ctm, formatMapper = {
            try {
                // Document conversions.
                val dtm = ctm.documentMetadata(it.name)
                return@getZipped if (shouldMerge && mergeFormatMatches(it, format)) {
                    logger.info("Merging ${it.name} of format ${it.format}")
                    mergeDoc(dtm, posHeadOnly).file
                } else {
                    logger.info("Converting ${it.name} of format ${it.format} to $format")
                    convertDoc(dtm, posHeadOnly)
                }
            } catch (e: MergeNotImplementedException) {
                throw e
            } catch (e: Exception) {
                throw Exception("Could not convert file ${it.name} to format ${format}. ${e.message}.")
            }
        }, filter = {
            // Filter out untagged documents.
                document ->
            ctm.documentMetadata(document.name).layer != Layer.EMPTY
        }, outputStream = response?.outputStream)
    }

    private fun getCorpusTransformMetadata(
        corpusID: UUID,
        jobName: String,
        formatName: DocumentFormat,
    ): CorpusTransformMetadata {
        // Exporting documents requires you to have write access.
        val corpus = corpora.getWriteAccessOrThrow(corpusID, request)
        val job = corpus.jobs.readOrThrow(jobName)
        return CorpusTransformMetadata(
            corpus, job, User.getUserFromRequestOrThrow(request), formatName
        )
    }

    private fun getDocumentTransformMetadata(
        corpus: UUID,
        job: String,
        document: String,
        format: DocumentFormat,
    ): DocumentTransformMetadata {
        return getCorpusTransformMetadata(corpus, job, format).documentMetadata(document)
    }

    private fun mergeFormatMatches(
        it: Document, format: DocumentFormat,
    ): Boolean {
        var otherFormat = it.format
        // Overwrite the format for legacy formats that can in fact be merged.
        if (otherFormat == DocumentFormat.TeiP5Legacy) {
            otherFormat = DocumentFormat.TeiP5
        }
        return otherFormat == format
    }

    fun getCorpusName(corpus: UUID): String {
        return corpora.getWriteAccessOrThrow(corpus, request).metadata.expensiveGet().name
    }


}