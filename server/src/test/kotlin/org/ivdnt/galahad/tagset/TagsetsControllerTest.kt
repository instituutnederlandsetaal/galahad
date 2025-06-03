package org.ivdnt.galahad.tagset

import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.web.controller.TagsetsController
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [Galahad::class])
class TagsetsControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val ctrl: TagsetsController,
) {

    @Test
    fun `Get valid tagset`() {
        val tagset = ctrl.getTagset("TDN-Core")
        assertNotNull(tagset)
        assertTrue("PC" in tagset.punctuationTags)
    }

    @Test
    fun `Get invalid tagset`() {
        assertThrows(Exception::class.java) { ctrl.getTagset("invalid") }
    }

    @Test
    fun `Get tagsets`() {
        val tagsets = ctrl.getTagsets()
        assertEquals(1, tagsets.count { it.id == "TDN-Core" })
    }
}