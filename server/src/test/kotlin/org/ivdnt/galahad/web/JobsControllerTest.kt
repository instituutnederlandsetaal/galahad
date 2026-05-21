package org.ivdnt.galahad.web

import com.github.tomakehurst.wiremock.client.WireMock.*
import java.util.*
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.ivdnt.galahad.exceptions.UserUnauthorizedException
import org.ivdnt.galahad.jobs.JobMetadata
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil
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
        private fun assertCanGetJob(user: String) {
            val corpus = TestUtil.createFilledCorpus(config)
            val metadata: JobMetadata =
                performGetJob(corpus.uuid, user)
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                    }
                    .andReturn()
                    .andDeserialize()
            val expectedTagger = Tagger.readOrThrow(TestUtil.TAGGER)
            val untagged = TestUtil.get("formats/shared/converter").listFiles().size
            assertEquals(expectedTagger, metadata.tagger)
            assertEquals(untagged, metadata.progress.untagged)
        }

        @Test
        fun `Owner can get job`() {
            assertCanGetJob(TestUtil.USER)
        }

        @Test
        fun `Admin can get job`() {
            assertCanGetJob("admin")
        }

        @Test
        fun `Collaborator can get job`() {
            assertCanGetJob("collaborator")
        }

        @Test
        fun `Viewer can get job`() {
            assertCanGetJob("viewer")
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
            performGetJob(UUID.randomUUID(), tagger = "non-existing").andExpect {
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
    inner class JobDeletionTest {
        private fun assertCanDeleteJob(user: String) {
            val corpus = TestUtil.createJobbedCorpus(config)
            assertEquals(0, getJobs(corpus).sumOf { it.progress.untagged })
            assert(0 != getJobs(corpus).sumOf { it.progress.finished })
            performDeleteJob(corpus.uuid, user).andExpect { status { isNoContent() } }
            assertEquals(0, getJobs(corpus).sumOf { it.progress.finished })
            assert(0 != getJobs(corpus).sumOf { it.progress.untagged })
        }

        @Test
        fun `Owner can delete job`() {
            assertCanDeleteJob(TestUtil.USER)
        }

        @Test
        fun `Admin can delete job`() {
            assertCanDeleteJob("admin")
        }

        @Test
        fun `Collaborator can delete job`() {
            assertCanDeleteJob("collaborator")
        }
    }

    private fun performGetJob(
        corpus: UUID,
        user: String = TestUtil.USER,
        tagger: String = TestUtil.TAGGER,
    ): ResultActionsDsl =
        mvc.get("/corpora/${corpus}/jobs/$tagger") { headers { assignHeaders(this, user) } }

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
