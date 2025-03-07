package org.ivdnt.galahad.web.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.documents.Document
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.annotations.Layer
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

    private val user get() = User.fromRequest(request)


    fun mergeDoc(corpus: UUID, job: String, document: String, posHeadOnly: Boolean): InternalFile {
        val doc = corpora.readAsWriterOrThrow(corpus, user).documents.readOrThrow(document)
        val dtm = getDocumentTransformMetadata(corpus, job, document, doc.metadata.format)
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
        ctm.corpus.export(ctm, formatMapper = {
            try {
                // Document conversions.
                val dtm = ctm.documentMetadata(it.name)
                return@export if (shouldMerge && mergeFormatMatches(it, format)) {
                    logger.info("Merging ${it.name} of format ${it.metadata.format}")
                    mergeDoc(dtm, posHeadOnly).file
                } else {
                    logger.info("Converting ${it.name} of format ${it.metadata.format} to $format")
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
        val corpus = corpora.readAsWriterOrThrow(corpusID, user)
        val job = corpus.jobs.readOrThrow(jobName)
        return CorpusTransformMetadata(
            corpus, job, User.fromRequest(request), formatName
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
        var otherFormat = it.metadata.format
        // Overwrite the format for legacy formats that can in fact be merged.
        if (otherFormat == DocumentFormat.TeiP5Legacy) {
            otherFormat = DocumentFormat.TeiP5
        }
        return otherFormat == format
    }

    fun getCorpusName(corpus: UUID): String {
        return corpora.readAsWriterOrThrow(corpus, user).mutableMetadata.name
    }
}