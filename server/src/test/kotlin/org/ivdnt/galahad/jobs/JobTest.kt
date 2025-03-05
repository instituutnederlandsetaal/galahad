package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.evaluation.metrics.FlatMetricType
import org.ivdnt.galahad.formats.LayerBuilder
import org.ivdnt.galahad.formats.createCorpus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class JobTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Create a job`() {
        val job: Job = corpus.jobs.createOrThrow(TestConfig.TAGGER_NAME)
        // verify
        assertEquals(TestConfig.TAGGER_NAME, job.name)
        assertFalse(job.isActive)
        assertEquals(0, job.assay.readOrThrow<Map<String, FlatMetricType>>().size)
        assertEquals(0, job.progress.total)
        assertEquals(LayerPreview.EMPTY, job.preview)
        // verify from state cache
        assertEquals(LayerPreview.EMPTY, job.metadata.preview)
        assertEquals(0, job.metadata.progress.total)
        assertEquals(0, job.metadata.resultSummary.numTerms)
    }

    @Test
    fun `Fake tagger result`() {
        // add a doc
        val doc = corpus.documents.createOrThrow(File.createTempFile("tmp", ".txt"))
        // create a job
        val job: Job = corpus.jobs.createOrThrow(TestConfig.TAGGER_NAME)
        // fake a tagger result
        val layer = LayerBuilder().loadDummies(100).build()
        job.createOrThrow(doc.name, layer)
        // verify
        assertEquals(100, job.readOrThrow(doc).result.terms.size)
        assertEquals(1, job.progress.finished)
    }
}