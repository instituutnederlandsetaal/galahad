package org.ivdnt.galahad.jobs
//
//import java.io.File
//import org.ivdnt.galahad.annotations.Annotation
//import org.ivdnt.galahad.annotations.LayerPreview
//import org.ivdnt.galahad.corpora.Corpus
//import org.ivdnt.galahad.util.LayerBuilder
//import org.ivdnt.galahad.util.TestUtil
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertFalse
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//
//class JobTest {
//    lateinit var corpus: Corpus
//
//    @BeforeEach
//    fun initCorpus() {
//        corpus = TestUtil.createCorpus()
//    }
//
//    @Test
//    fun `Create a job`() {
//        val job: Job = corpus.jobs.createOrThrow(TestUtil.TAGGER_NAME)
//        // verify
//        assertEquals(TestUtil.TAGGER_NAME, job.name)
//        assertFalse(job.isActive)
//        assertEquals(0, job.progress.total)
//        // verify from state cache
//        assertEquals(LayerPreview.EMPTY, job.metadata.preview)
//        assertEquals(0, job.metadata.progress.total)
//        assertEquals(null, job.metadata.annotations.annotations[Annotation.TOKEN])
//    }
//
//    @Test
//    fun `Fake tagger result`() {
//        // add a doc
//        val doc = corpus.documents.createOrThrow(File.createTempFile("tmp", ".txt"))
//        // create a job
//        val job: Job = corpus.jobs.createOrThrow(TestUtil.TAGGER_NAME)
//        // fake a tagger result
//        val layer = LayerBuilder().loadDummies(100).build()
//        job.setLayer(doc.name, layer)
//        // verify
//        assertEquals(100, job.getLayer(doc).terms.count())
//        assertEquals(1, job.progress.finished)
//    }
//}
