package org.ivdnt.galahad.web

import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil.assignHeaders
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
class ApplicationControllerTest(@Autowired val mvc: MockMvc) {

    fun assertGetUser(username: String, admin: Boolean) {
        val user: User =
            mvc.get("/user") { headers { assignHeaders(this, username) } }
                .andExpect { status { isOk() } }
                .andReturn()
                .andDeserialize()
        assertEquals(username, user.id)
        assertEquals(admin, user.admin)
    }

    @Test
    fun `Get non-admin user`() {
        assertGetUser("non-admin", false)
    }

    @Test
    fun `Get admin user`() {
        assertGetUser("admin", true)
    }

    @Test
    fun `Get default user when user header is missing`() {
        val user: User =
            mvc.get("/user").andExpect { status { isOk() } }.andReturn().andDeserialize()
        assertEquals(User.DEFAULT_USER.id, user.id)
        assertEquals(User.DEFAULT_USER.admin, user.admin)
    }
}
