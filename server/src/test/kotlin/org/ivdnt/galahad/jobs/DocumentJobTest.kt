package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.util.LayerBuilder
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

class DocumentJobTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    @Test
    fun `Create DocumentJob`() {
        // add a doc
        val doc = corpus.documents.createOrThrow(File.createTempFile("tmp", ".txt"))
        // create a job
        val job: Job = corpus.jobs.createOrThrow(TestUtil.TAGGER_NAME)
        // create a document job
        val dj: JobResult = job.results.createOrThrow(doc.name)
        // verify
        assertEquals(doc.name, dj.name)
        assertNull(dj.error)
        assertNull(dj.processingID)
        assertFalse(dj.isProcessing)
        assertEquals(null, dj.layer)
        assertEquals(JobStatus.PENDING, dj.status)

        // set error
        dj.error = "error"
        assertEquals("error", dj.error)
        assertEquals(JobStatus.ERROR, dj.status)

        // setting pid should delete error
        val id = UUID.randomUUID()
        dj.processingID = id
        assertNull(dj.error)
        assertEquals(id, dj.processingID)
        assertEquals(JobStatus.PROCESSING, dj.status)

        // Cancel should delete pid
        dj.cancel()
        assertNull(dj.processingID)
        assertNull(dj.error)
        assertEquals(JobStatus.PENDING, dj.status)

        // set result should finish
        val layer = LayerBuilder().loadDummies(100).build()
        dj.layer = layer
        assertEquals(100, dj.layer!!.terms.count())
        assertNull(dj.processingID)
        assertNull(dj.error)
        assertEquals(JobStatus.FINISHED, dj.status)

    }
}