package org.ivdnt.galahad.export

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.documents.Document
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.corpora.documents.Documents
import org.ivdnt.galahad.corpora.jobs.Job
import org.ivdnt.galahad.exceptions.MergeNotImplementedException
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.createZipFile
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import kotlin.io.path.createTempDirectory

class CorpusExport private constructor(
    val corpus: Corpus,
    val job: Job,
    val user: User,
    val format: DocumentFormat,
    val posHeadOnly: Boolean,
    val tagger: Tagger,
    val shouldMerge: Boolean,
) : Logging {

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


    fun formatMapper(doc: Document): File {
        try {
            // Document conversions.
            val docExport = DocumentExport.create(this, doc)
            return if (shouldMerge && mergeFormatMatches(doc, format)) {
                logger.info("Merging ${doc.name} of format ${doc.metadata.format}")
                docExport.merge()
            } else {
                logger.info("Converting ${doc.name} of format ${doc.metadata.format} to $format")
                docExport.convert()
            }
        } catch (e: MergeNotImplementedException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Could not convert file ${doc.name} to format ${format}. ${e.message}.")
        }
    }

    /**
     * Maps all [Document] found in [Documents] to the desired [DocumentFormat] and zips them. [formatMapper] should perform the mapping.
     */
    fun export(
        out: OutputStream,
    ) {
        val documents = corpus.documents.readAll().filter { DocumentExport.create(this, it).layer != Layer.EMPTY }
        val convertedDocs = documents.asSequence().map(::formatMapper)
        val docsToCmdi = documents.asSequence().map { CmdiMetadata(DocumentExport.create(this, it)).file }
        val cmdiZip = createZipFile(docsToCmdi, includeCMDI = true)
        // rename the cmdiZip to "metadata"
        val dest = createTempDirectory("metadata").toFile().resolve("metadata.zip")
        Files.move(cmdiZip.toPath(), dest.toPath())
        createZipFile(convertedDocs + dest, out)
    }

    companion object {
        fun create(
            corpus: Corpus,
            jobName: String,
            format: DocumentFormat,
            posHeadOnly: Boolean,
            user: User,
            shouldMerge: Boolean
        ): CorpusExport = CorpusExport(
            corpus = corpus,
            job = corpus.jobs.readOrThrow(jobName),
            user = user,
            format = format,
            posHeadOnly = posHeadOnly,
            tagger = Tagger.readOrThrow(jobName, corpus),
            shouldMerge = shouldMerge
        )
    }
}