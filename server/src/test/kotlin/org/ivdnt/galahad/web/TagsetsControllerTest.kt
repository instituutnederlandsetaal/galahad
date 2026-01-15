package org.ivdnt.galahad.web

import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.web.controller.TagsetsController
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [Galahad::class])
class TagsetsControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val ctrl: TagsetsController,
) {
    @Test
    fun `Get tagsets`() {
        val tagsets = ctrl.getTagsets()
        Assertions.assertEquals(1, tagsets.count { it.name == "TDN-Core" })
    }
}