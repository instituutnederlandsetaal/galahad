package org.ivdnt.galahad.web

import com.github.tomakehurst.wiremock.client.WireMock.*
import java.util.*
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.jobs.JobMetadata
import org.ivdnt.galahad.util.*
import org.ivdnt.galahad.util.TestUtil.assignHeaders
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
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
    inner class JobCreationTest {
        @Test
        fun `Can create job`() {
            stubFor(post("/input").willReturn(ok().withBody(UUID.randomUUID().toString())))
            val corpus = TestUtil.createFilledCorpus(config)
            var jobs: List<JobMetadata> =
                mvc.get("/corpora/${corpus.uuid}/jobs") { headers(::assignHeaders) }
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                    }
                    .andReturn()
                    .andDeserialize()
            assertEquals(0, jobs.sumOf { it.progress.processing })
            mvc.post("/corpora/${corpus.uuid}/jobs/${TestUtil.TAGGER_NAME}") {
                    headers(::assignHeaders)
                }
                .andExpect { status { isAccepted() } }
            jobs =
                mvc.get("/corpora/${corpus.uuid}/jobs") { headers(::assignHeaders) }
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                    }
                    .andReturn()
                    .andDeserialize()
            assert(0 != jobs.sumOf { it.progress.processing })
        }
    }
}
