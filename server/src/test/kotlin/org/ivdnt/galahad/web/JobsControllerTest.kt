package org.ivdnt.galahad.web

import com.github.tomakehurst.wiremock.client.WireMock.*
import java.util.*
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.ivdnt.galahad.exceptions.UserUnauthorizedException
import org.ivdnt.galahad.jobs.JobMetadata
import org.ivdnt.galahad.jobs.Progress
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.util.TestUtil.WEB_CORPUS
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.ivdnt.galahad.util.andDeserialize
import org.ivdnt.galahad.web.controller.ErrorController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.*
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock

/** Web controller tests for serialization, status, exception resolving and permissions. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@Import(ErrorController::class)
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
@EnableWireMock(ConfigureWireMock(name = "my-mock", port = 8102))
class JobsControllerTest(@Autowired val mvc: MockMvc, @Autowired val config: Config) {

    @Nested
    inner class JobGetTest {
        private fun assertGetJob(user: String, dataset: Boolean = false) {
            val corpus = TestUtil.createFilledCorpus(config, dataset)
            val metadata: JobMetadata =
                performGetJob(corpus.uuid, user)
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                    }
                    .andReturn()
                    .andDeserialize()
            val expectedTagger = Tagger.readOrThrow(TestUtil.TAGGER)
            val untagged = TestUtil.get(WEB_CORPUS).listFiles().size
            assertEquals(expectedTagger, metadata.tagger)
            assertEquals(untagged, metadata.progress.untagged)
        }

        @Test
        fun `Owner can get job`() {
            assertGetJob(TestUtil.USER)
        }

        @Test
        fun `Admin can get job`() {
            assertGetJob("admin")
        }

        @Test
        fun `Collaborator can get job`() {
            assertGetJob("collaborator")
        }

        @Test
        fun `Viewer can get job`() {
            assertGetJob("viewer")
        }

        @Test
        fun `Stranger can get job from dataset`() {
            assertGetJob("viewer", dataset = true)
        }

        @Test
        fun `Stranger can't get job`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performGetJob(corpus.uuid, "stranger").andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
        }

        @Test
        fun `Can't get job for non-existing corpus`() {
            performGetJob(UUID.randomUUID()).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }

        @Test
        fun `Can't get job for non-existing tagger`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performGetJob(corpus.uuid, tagger = "non-existing").andExpect {
                status { isNotFound() }
                match { it.resolvedException is TaggerNotFoundException }
            }
        }
    }

    @Nested
    inner class JobCreationTest {
        private fun assertCreateJob(user: String) {
            stubFor(post("/input").willReturn(ok().withBody(UUID.randomUUID().toString())))
            val corpus = TestUtil.createFilledCorpus(config)
            assertEquals(0, getJobs(corpus).sumOf { it.progress.processing })
            performCreateJob(corpus.uuid, user).andExpect { status { isAccepted() } }
            assert(0 != getJobs(corpus).sumOf { it.progress.processing })
        }

        @Test
        fun `Owner can create job`() {
            assertCreateJob(TestUtil.USER)
        }

        @Test
        fun `Admin can create job`() {
            assertCreateJob("admin")
        }

        @Test
        fun `Collaborator can create job`() {
            assertCreateJob("collaborator")
        }

        private fun assertCantCreateJob(user: String) {
            val corpus = TestUtil.createFilledCorpus(config)
            performCreateJob(corpus.uuid, user).andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
            assertEquals(0, getJobs(corpus).sumOf { it.progress.processing })
        }

        @Test
        fun `Viewer can't create job`() {
            assertCantCreateJob("viewer")
        }

        @Test
        fun `Stranger can't create job`() {
            assertCantCreateJob("stranger")
        }

        @Test
        fun `Can't create job in non-existing corpus`() {
            performCreateJob(UUID.randomUUID()).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }

        @Test
        fun `Can't create job for non-existing tagger`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performCreateJob(corpus.uuid, tagger = "non-existing").andExpect {
                status { isNotFound() }
                match { it.resolvedException is TaggerNotFoundException }
            }
            assertEquals(0, getJobs(corpus).sumOf { it.progress.processing })
        }
    }

    @Nested
    inner class JobProgressTest {
        private fun assertGetJobProgress(user: String, dataset: Boolean = false) {
            val corpus = TestUtil.createJobbedCorpus(config, dataset)
            val progress: Progress =
                performGetJobProgress(corpus.uuid, user)
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                    }
                    .andReturn()
                    .andDeserialize()
            assertEquals(1, progress.finished)
        }

        @Test
        fun `Owner can get job progress`() {
            assertGetJobProgress(TestUtil.USER)
        }

        @Test
        fun `Admin can get job progress`() {
            assertGetJobProgress("admin")
        }

        @Test
        fun `Collaborator can get job progress`() {
            assertGetJobProgress("collaborator")
        }

        @Test
        fun `Viewer can get job progress`() {
            assertGetJobProgress("viewer")
        }

        @Test
        fun `Stranger can get job progress from dataset`() {
            assertGetJobProgress("viewer", dataset = true)
        }

        @Test
        fun `Stranger can't get job progress`() {
            val corpus = TestUtil.createJobbedCorpus(config)
            performGetJobProgress(corpus.uuid, "stranger").andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
        }

        @Test
        fun `Can't get job progress for non-existing corpus`() {
            performGetJobProgress(UUID.randomUUID()).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }

        @Test
        fun `Can't get job progress for non-existing tagger`() {
            val corpus = TestUtil.createJobbedCorpus(config)
            performGetJobProgress(corpus.uuid, tagger = "non-existing").andExpect {
                status { isNotFound() }
                match { it.resolvedException is TaggerNotFoundException }
            }
        }

        @Test
        fun `Can't get job progress for existing tagger without job`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performGetJobProgress(corpus.uuid, tagger = TestUtil.TAGGER).andExpect {
                status { isNotFound() }
                match { it.resolvedException is JobNotFoundException }
            }
        }
    }

    @Nested
    inner class JobDeletionTest {
        private fun assertDeleteJob(user: String) {
            val corpus = TestUtil.createJobbedCorpus(config)
            assertEquals(1, getJobs(corpus).sumOf { it.progress.finished })
            performDeleteJob(corpus.uuid, user).andExpect { status { isNoContent() } }
            // finished documents are still finished
            assertEquals(1, getJobs(corpus).sumOf { it.progress.finished })
        }

        @Test
        fun `Owner can delete job`() {
            assertDeleteJob(TestUtil.USER)
        }

        @Test
        fun `Admin can delete job`() {
            assertDeleteJob("admin")
        }

        @Test
        fun `Collaborator can delete job`() {
            assertDeleteJob("collaborator")
        }

        private fun assertCantDeleteJob(user: String) {
            val corpus = TestUtil.createJobbedCorpus(config)
            assert(0 != getJobs(corpus).sumOf { it.progress.finished })
            performDeleteJob(corpus.uuid, user).andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
            assert(0 != getJobs(corpus).sumOf { it.progress.finished })
        }

        @Test
        fun `Viewer can't delete job`() {
            assertCantDeleteJob("viewer")
        }

        @Test
        fun `Stranger can't delete job`() {
            assertCantDeleteJob("stranger")
        }

        @Test
        fun `Can't delete job for non-existing corpus`() {
            performDeleteJob(UUID.randomUUID()).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }

        @Test
        fun `Can't delete job for non-existing tagger`() {
            val corpus = TestUtil.createJobbedCorpus(config)
            performDeleteJob(corpus.uuid, tagger = "non-existing").andExpect {
                status { isNotFound() }
                match { it.resolvedException is TaggerNotFoundException }
            }
        }

        @Test
        fun `Can't delete job for existing tagger without job`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performDeleteJob(corpus.uuid, tagger = TestUtil.TAGGER).andExpect {
                status { isNotFound() }
                match { it.resolvedException is JobNotFoundException }
            }
        }
    }

    private fun performGetJob(
        corpus: UUID,
        user: String = TestUtil.USER,
        tagger: String = TestUtil.TAGGER,
    ): ResultActionsDsl =
        mvc.get("/corpora/${corpus}/jobs/$tagger") { headers { assignHeaders(this, user) } }

    private fun performGetJobProgress(
        corpus: UUID,
        user: String = TestUtil.USER,
        tagger: String = TestUtil.TAGGER,
    ): ResultActionsDsl =
        mvc.get("/corpora/${corpus}/jobs/$tagger/progress") {
            headers { assignHeaders(this, user) }
        }

    private fun performCreateJob(
        corpus: UUID,
        user: String = TestUtil.USER,
        tagger: String = TestUtil.TAGGER,
    ): ResultActionsDsl =
        mvc.post("/corpora/${corpus}/jobs/$tagger") { headers { assignHeaders(this, user) } }

    private fun performDeleteJob(
        corpus: UUID,
        user: String = TestUtil.USER,
        tagger: String = TestUtil.TAGGER,
    ): ResultActionsDsl =
        mvc.delete("/corpora/${corpus}/jobs/$tagger") { headers { assignHeaders(this, user) } }

    private fun performGetJobs(corpus: Corpus, user: String = TestUtil.USER): ResultActionsDsl =
        mvc.get("/corpora/${corpus.uuid}/jobs") { headers { assignHeaders(this, user) } }

    private fun getJobs(corpus: Corpus, user: String = TestUtil.USER): List<JobMetadata> =
        performGetJobs(corpus, user)
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()
            .andDeserialize()
}
