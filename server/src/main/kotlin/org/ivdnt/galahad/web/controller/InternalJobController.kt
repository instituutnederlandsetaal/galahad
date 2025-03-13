package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Hidden
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.INTERNAL_JOBS_ERROR_URL
import org.ivdnt.galahad.app.INTERNAL_JOBS_RESULT_URL
import org.ivdnt.galahad.corpora.documents.Document
import org.ivdnt.galahad.corpora.jobs.Job
import org.ivdnt.galahad.exceptions.InvalidDocumentFormatException
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.formats.conllu.ConlluFile
import org.ivdnt.galahad.formats.tsv.TSVFile
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.taggers.Tagset
import org.ivdnt.galahad.web.service.CorporaService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*

typealias ProcessingID = UUID
typealias CorpusID = UUID
typealias JobName = String
typealias DocumentName = String

@RestController
@Hidden
class InternalJobController(
    val corpora: CorporaService,
    val config: Config,
) : Logging {

    // This is not an efficient implementation. TODO: efficient implementation
    private fun dataForProcessingID(processingID: UUID): Triple<CorpusID, JobName, DocumentName>? {
        corpora.all.forEach { corpus ->
            corpus.jobs.readAll().forEach { job ->
                val candidate = job.documentNameForProcessingIDOrNull(processingID)
                if (candidate != null) return Triple(corpus.uuid, job.name, candidate)
            }
        }
        return null
    }

    /**
     * This is a special endpoint, as it is not for use with the client,
     * but for use with the taggers
     */
    @PostMapping(INTERNAL_JOBS_RESULT_URL)
    fun receiveTaggerResult(
        @RequestParam(value = "file_id", required = false) fileId: UUID,
        @RequestBody file: MultipartFile,
    ): String {
        logger.info("Received result with processing id $fileId")
        return try {
            // TODO remove the processing ID after processing
            val tempFile = File.createTempFile("job", file.originalFilename!!)
            file.transferTo(tempFile)
            val (corpusID, jobName, documentName) = dataForProcessingID(fileId)
                ?: throw Exception("Processing ID not found, was this file uploaded by me?")
            val original: Document = corpora.readCorpusUnsafe(corpusID).documents.readOrThrow(documentName)
            val job: Job = corpora.readCorpusUnsafe(corpusID).jobs.readOrThrow(jobName)
            val tagger = Tagger.readOrThrow(job.name)
            val tagset: Tagset? = Tagset.readOrNull(tagger)
            when (val uploadedFile = InternalFile.create(tempFile)) {
                // Treat TSVFiles separately form SourceLayerableFiles, because calling sourceLayer() on a TSV
                // Would default its alignment to offset=0. Instead, we force it to align with the original plaintext.
                is ConlluFile -> {
                    val alignedLayer = uploadedFile.mapOnPlainText(
                        plaintext = original.plaintext, layerName = jobName
                    )
                    job.jobDocuments.readOrThrow(documentName).layer = Layer(
                        name = jobName,
                        tagset = tagset ?: Tagset.UNKNOWN,
                        wordForms = alignedLayer.wordForms,
                        terms = alignedLayer.terms
                    )

                }

                is TSVFile -> {
                    val alignedLayer = uploadedFile.mapOnPlainText(
                        plaintext = original.plaintext, layerName = jobName
                    )
                    job.jobDocuments.readOrThrow(documentName).layer = Layer(
                        name = jobName,
                        tagset = tagset ?: Tagset.UNKNOWN,
                        wordForms = alignedLayer.wordForms,
                        terms = alignedLayer.terms
                    )
                }

                else -> {
                    throw InvalidDocumentFormatException("File type not supported")
                }
            }
            // If this was the last file, set active false
            if (job.progress.pending == 0 && job.progress.processing == 0) {
                job.isActive = false
            }
            job.next() // send new files
            "DELETE"
        } catch (e: Exception) {
            // Something went wrong, let the tagger keep the file for investigation
            // Alternatively, the user stopped the original job so there is nowhere to return to.
            logger.error("Could not receive tagger result. Exception $e")
            "KEEP"
        }
    }

    /**
     * This is a special endpoint, as it is not for use with the client,
     * but for use with the taggers
     */
    @PostMapping(INTERNAL_JOBS_ERROR_URL)
    fun receiveTaggerError(
        @RequestParam(value = "file_id") fileId: UUID,
        @RequestBody message: String,
    ): String {
        logger.info("Received error with processing id $fileId: $message")
        val (corpusID, jobName, documentName) = dataForProcessingID(fileId)
            ?: throw Exception("Processing ID not found, was this file uploaded by me?")
        corpora.readCorpusUnsafe(corpusID).jobs.readOrThrow(jobName).jobDocuments.readOrThrow(documentName).error =
            message

        // TODO Even thought we had an error, we can consider job.next() here
        return "KEEP" // or "DELETE"
    }

}