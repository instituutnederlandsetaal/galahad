package org.ivdnt.galahad.web

import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.util.andDeserialize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/** Web controller tests for serialization, status, exception resolving and permissions if applicable. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class TaggersControllerTest(
    @Autowired val mvc: MockMvc,
) {
    @Test
    fun `Can get taggers`() {
        val taggers: List<Tagger> = mvc.get("/taggers").andReturn().andDeserialize()
        assertEquals(1, taggers.count { it.id == TestUtil.TAGGER_NAME })
        assert(taggers.sumOf { it.attributions.size } > 0)
        assert(taggers.sumOf { it.annotations.sumOf { it.principles?.size ?: 0 } } > 0)
    }

    @Test
    fun `Can't get health of invalid tagger`() {
        mvc.get("/taggers/invalid/health").andExpect {
            status { isNotFound() }
            match { it.resolvedException is TaggerNotFoundException }
        }
    }
}