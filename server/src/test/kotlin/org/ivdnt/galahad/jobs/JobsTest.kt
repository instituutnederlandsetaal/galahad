package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.TestUtil

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JobsTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    @Test
    fun `Create a job`() {
        val name = TestConfig.TAGGER_NAME
        // Check if empty
        assertEquals(0, corpus.jobs.readAll().size)
        val numTaggers = Tagger.taggers.size + 1 // +1 for source layer
        assertEquals(numTaggers, corpus.jobs.readAllMetadata().size)
        assertNull(corpus.jobs.readOrNull(name))
        assertThrows(Exception::class.java) { corpus.jobs.readOrThrow(name) }
        // Create
        val job = corpus.jobs.createOrThrow(name)
        // Check if created
        assertNotNull(job)
        assertEquals(1, corpus.jobs.readAll().size)
        assertEquals(numTaggers, corpus.jobs.readAllMetadata().size)
        assertNotNull(corpus.jobs.readOrNull(name))
        assertNotNull(corpus.jobs.readOrThrow(name))
        // delete
        corpus.jobs.deleteOrThrow(name)
        // Check if deleted
        assertEquals(0, corpus.jobs.readAll().size)
        assertNull(corpus.jobs.readOrNull(name))
        assertThrows(Exception::class.java) { corpus.jobs.readOrThrow(name) }
    }
}