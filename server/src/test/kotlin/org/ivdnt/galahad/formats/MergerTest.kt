package org.ivdnt.galahad.formats

import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.export.CorpusExport
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayOutputStream
import java.io.File

abstract class MergerTest {
    private lateinit var corpus: Corpus
    abstract val folder: String
    abstract val format: DocumentFormat

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    fun merge() {
        val input: File = TestUtil.get("formats/$folder/input.${format.extension}")
        val merge: File = TestUtil.get("formats/$folder/layer.conllu") // TODO perhaps use json
        val output: File = TestUtil.get("formats/$folder/output.${format.extension}")

        val doc = corpus.documents.createOrThrow(input)
        // set merge layer as a job
        val job = corpus.jobs.createOrThrow("spacy")
        job.setLayer(doc, InternalFile.create(merge).layer)

        // merge
        val corpusExport = CorpusExport.create(corpus, "spacy", format, User.DEFAULT_USER, true, false)
        val docExport = corpusExport.documentExport(doc)

        val convertedText = ByteArrayOutputStream().also { docExport.merge(it); it.flush() }.toString()
        val expectedText = output.readText()

        Assertions.assertEquals(expectedText, convertedText)
    }
}