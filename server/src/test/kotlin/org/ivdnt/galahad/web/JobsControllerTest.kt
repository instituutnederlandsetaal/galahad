package org.ivdnt.galahad.web

import org.ivdnt.galahad.app.Config
import java.io.File
import java.util.*
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.corpora.CorpusStatistics
import org.ivdnt.galahad.exceptions.CorpusInvalidException
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.CorpusUnauthorizedException
import org.ivdnt.galahad.util.*
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.ivdnt.galahad.web.controller.ErrorController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import tools.jackson.module.kotlin.convertValue

/** Web controller tests for serialization, status, exception resolving and permissions. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@Import(ErrorController::class)
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class JobsControllerTest(@Autowired val mvc: MockMvc, @Autowired val config: Config) {
    @Nested
    inner class JobCreationTest {
        @Test
        fun `Can create job`() {
            val corpus = TestUtil.createFilledCorpus(config)
            mvc.post("/corpora/${corpus.uuid}/jobs/${TestUtil.TAGGER_NAME}") { headers(::assignHeaders) } .andExpect { status { isAccepted() } }
        }
    }
}