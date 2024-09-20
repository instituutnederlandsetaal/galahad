package org.ivdnt.galahad.data

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.JOB_DOCUMENT_URL
import org.ivdnt.galahad.app.JOB_URL
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.exceptions.MergeNotImplementedException
import org.ivdnt.galahad.port.CorpusTransformMetadata
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.InternalFile
import org.ivdnt.galahad.util.setContentDisposition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.File
import java.util.*

@RestController
class ExportController(
    val corpora: CorporaService,
) : Logging {

    @Autowired
    private val request: HttpServletRequest? = null

    @Autowired
    private val response: HttpServletResponse? = null

    private fun getCorpusTransformMetadata(
        corpusID: UUID,
        jobName: String,
        formatName: DocumentFormat,
    ): CorpusTransformMetadata {
        // Exporting documents requires you to have write access.
        val corpus = corpora.getWriteAccessOrThrow(corpusID, request)
        val job = corpus.jobs.readOrThrow(jobName)
        return CorpusTransformMetadata(
            corpus = corpus, job = job, user = User.getUserFromRequestOrThrow(request), targetFormat = formatName
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

    @Operation(
        summary = "Convert all job documents",
        description = "Convert and export all documents of a tagger job to a specific format",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "The converted documents in a zip file (.zip). Only includes tagged documents.",
                content = [Content(mediaType = "*/*")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Corpus or job not found",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid format",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            ),
            ApiResponse(
                responseCode = "403",
                description = "The user is not allowed to export files. Exporting requires write-access.",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            )
        ]
    )
    @CrossOrigin
    @ResponseBody
    @GetMapping("$JOB_URL/export/convert")
    fun convertAndExportJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger ID") job: String,
        @RequestParam("format") @Parameter(description = "Export format") formatName: String,
        @RequestParam("posHeadOnly") @Parameter(description = "Only export the head of PoS (e.g. PD in PD(type=art))") posHeadOnly: Boolean = false,
    ) {
        return exportCorpusJobInFormat(corpus, job, formatName, shouldMerge = false, posHeadOnly)
    }

    @Operation(
        summary = "Merge all job document",
        description = "Merge and export all document of a tagger job to a specific format. Any documents matching the selected format will be merged, others simply converted.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "The converted documents in a zip file (.zip). Only includes tagged documents.",
                content = [Content(mediaType = "*/*")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Corpus or job not found",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            ),
            ApiResponse(
                responseCode = "400",
                description = "The format is not supported for merging.",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            ),
            ApiResponse(
                responseCode = "403",
                description = "The user is not allowed to export files. Exporting requires write-access.",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            )
        ]
    )
    @CrossOrigin
    @ResponseBody
    @GetMapping("$JOB_URL/export/merge", produces = ["application/zip", "application/json"])
    fun mergeAndExportJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger ID") job: String,
        @RequestParam("format") @Parameter(description = "Export format")
        filterFormat: String,
        @RequestParam("posHeadOnly") @Parameter(description = "Only export the head of PoS (e.g. PD in PD(type=art))")
        posHeadOnly: Boolean,
    ) {
        return exportCorpusJobInFormat(corpus, job, filterFormat, shouldMerge = true, posHeadOnly)
    }

    fun exportCorpusJobInFormat(
        corpus: UUID,
        job: String,
        formatName: String,
        shouldMerge: Boolean,
        posHeadOnly: Boolean,
    ) {
        val format = DocumentFormat.fromString(formatName)
        val ctm = getCorpusTransformMetadata(corpus, job, format)
        setZipResponseHeader(ctm)
        ctm.corpus.getZipped(ctm, formatMapper = {
            try {
                // Document conversions.
                val dtm = ctm.documentMetadata(it.name)
                return@getZipped if (shouldMerge && mergeFormatMatches(it, format)) {
                    logger.info("Merging ${it.name} of format ${it.format}")
                    mergeAndExportDocument(dtm, posHeadOnly).file
                } else {
                    logger.info("Converting ${it.name} of format ${it.format} to $format")
                    convertAndExportDocument(dtm, format, posHeadOnly)
                }
            } catch (e: MergeNotImplementedException) {
                throw e
            }
            catch (e: Exception) {
                throw Exception("Could not convert file ${it.name} to format ${format}. ${e.message}.")
            }
        }, filter = {
            // Filter out untagged documents.
                document ->
            ctm.documentMetadata(document.name).layer != Layer.EMPTY
        }, outputStream = response?.outputStream)
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

    @Operation(
        summary = "Convert job document",
        description = "Convert and export a document to a specific format",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "The converted document",
                content = [Content(mediaType = "*/*")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Corpus, job or document not found",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid format",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            ),
            ApiResponse(
                responseCode = "403",
                description = "The user is not allowed to export files. Exporting requires write-access.",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            )
        ]
    )
    @CrossOrigin
    @GetMapping("$JOB_DOCUMENT_URL/export/convert")
    fun convertAndExportDocument(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger ID") job: String,
        @PathVariable @Parameter(description = "Document name") document: String,
        @RequestParam("format") @Parameter(description = "Export format") formatName: String,
        @RequestParam("posHeadOnly") @Parameter(description = "Only export the head of PoS (e.g. PD in PD(type=art))") posHeadOnly: Boolean,
    ): ByteArray? {
        response?.contentType = "text/plain"
        val format = DocumentFormat.fromString(formatName)
        val dtm = getDocumentTransformMetadata(corpus, job, document, format)
        return convertAndExportDocument(dtm, format, posHeadOnly).readBytes()
    }

    fun convertAndExportDocument(dtm: DocumentTransformMetadata, format: DocumentFormat, posHeadOnly: Boolean): File {
        if (posHeadOnly) {
            dtm.convertLayerToPosHead()
        }
        return dtm.document.generateAs(format, dtm)
    }

    @Operation(
        summary = "Merge job document",
        description = "Merge and export a document, retaining its original format.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "The converted document",
                content = [Content(mediaType = "*/*")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Corpus, job or document not found",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            ),
            ApiResponse(
                responseCode = "400",
                description = "The format of the original document is not supported for merging.",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            ),
            ApiResponse(
                responseCode = "403",
                description = "The user is not allowed to export files. Exporting requires write-access.",
                content = [Content(
                    array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)),
                )],
            )
        ]
    )
    @CrossOrigin
    @GetMapping("$JOB_DOCUMENT_URL/export/merge", produces = ["text/plain", "application/json"])
    fun mergeAndExportDocument(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger ID") job: String,
        @PathVariable @Parameter(description = "Document name") document: String,
        @RequestParam("posHeadOnly") @Parameter(description = "Only export the head of PoS (e.g. PD in PD(type=art))") posHeadOnly: Boolean,
    ): ByteArray? {
        response?.contentType = "text/plain"
        val doc = corpora.getWriteAccessOrThrow(corpus, request).documents.readOrThrow(document)
        val dtm = getDocumentTransformMetadata(corpus, job, document, doc.format)
        return mergeAndExportDocument(dtm, posHeadOnly).file.readBytes()
    }

    fun mergeAndExportDocument(dtm: DocumentTransformMetadata, posHeadOnly: Boolean): InternalFile {
        if (posHeadOnly) {
            dtm.convertLayerToPosHead()
        }
        return dtm.document.merge(dtm)
    }

    private fun setZipResponseHeader(ctm: CorpusTransformMetadata) {
        response!!.contentType = "application/zip"
        response.setContentDisposition(ctm.corpus.metadata.expensiveGet().name + ".zip")
    }
}
