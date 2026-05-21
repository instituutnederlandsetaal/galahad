package org.ivdnt.galahad.web

import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.exceptions.TagsetNotFoundException
import org.ivdnt.galahad.taggers.Tagset
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

/** Web controller tests for serialization, status, exception resolving and permissions. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class TagsetsControllerTest(@Autowired val mvc: MockMvc) {
    @Test
    fun `Can get tagsets`() {
        val tagsets: List<Tagset> =
            mvc.get("/tagsets").andExpect { status { isOk() } }.andReturn().andDeserialize()
        assertEquals(1, tagsets.count { it.name == TestUtil.TAGSET_NAME })
        assert(tagsets.sumOf { it.punctuation.size } > 0)
    }

    @Test
    fun `Can get single tagset`() {
        val tagset: Tagset =
            mvc.get("/tagsets/${TestUtil.TAGSET_NAME}")
                .andExpect { status { isOk() } }
                .andReturn()
                .andDeserialize()
        assertEquals(TestUtil.TAGSET_NAME, tagset.name)
    }

    @Test
    fun `Can't get invalid tagset`() {
        mvc.get("/tagsets/invalid").andExpect {
            status { isNotFound() }
            match { it.resolvedException is TagsetNotFoundException }
        }
    }
}
