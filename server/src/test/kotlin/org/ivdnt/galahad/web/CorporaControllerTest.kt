package org.ivdnt.galahad.web

import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.corpora.CorpusMetadata
import org.ivdnt.galahad.exceptions.CorpusNameInvalidException
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.CorpusUnauthorizedException
import org.ivdnt.galahad.util.*
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import java.util.*

/** Web controller tests for serialization, status, exception resolving and permissions if applicable. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class CorporaControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val config: Config,
) {

    @Test
    fun `retrieve non-existing corpus`() {
        mvc.get("/corpora/${UUID.randomUUID()}").andExpect {
            status { isNotFound() }
            match { it.resolvedException is CorpusNotFoundException }
        }
    }

    @Test
    fun `Create corpus with invalid name`() {
        val body = mapOf("name" to "   ") // blank string
        mvc.postJson("/corpora", body) {
            headers(::assignHeaders)
        }.andExpect {
            status { isBadRequest() }
            match { it.resolvedException is CorpusNameInvalidException }
        }
    }

    @Test
    fun `accessing endpoints on non-owned corpus should throw`() {
        val corpus = TestUtil.createEmptyCorpus(config)
        mvc.get("/corpora/${corpus.uuid}/documents") {
            headers { assignHeaders(this, "stranger") }
        }.andExpect {
            status { isForbidden() }
            match { it.resolvedException is CorpusUnauthorizedException }
        }
    }

    @Test
    fun `Create dataset corpus`() {
        val body = mapOf("name" to "test", "dataset" to true)
        // try as nonadmin
        mvc.postJson("/corpora", body) {
            headers { assignHeaders(this, "nonadmin") }
        }.andExpect {
            status { isForbidden() }
            match { it.resolvedException is CorpusUnauthorizedException }
        }
        // try as admin
        postCorpus(body, "admin")
    }

    @Test
    fun `Post and delete unicode corpus`() {
        val name = "日本語"
        val body = mapOf("name" to name)
        val uuid = postCorpus(body)
        val corpus = getCorpus(uuid)
        assertEquals(name, corpus.name)
        assertEquals(TestConfig.TEST_USER, corpus.owner)
        assertEquals(1, getAllCorpora().size)
        deleteCorpus(uuid)
        assertEquals(0, getAllCorpora().size)
    }

    @Test
    fun `Test owner, collaborators and viewers`() { // TODO: should split in separate tests
        val owner = "testUser"
        val collabs = mutableSetOf("collab1", owner)
        val viewers = mutableSetOf("viewer1", "collab1", owner)

        // Create
        val meta = mutableMapOf(
            "name" to "test",
            "collaborators" to collabs,
            "viewers" to viewers,
        )
        val uuid = postCorpus(meta)

        // Check if created correctly, i.e. no permission overlap.
        // first try to get it as a stranger
        assertGetCorpusThrows(uuid, "stranger")
        // then as a viewer
        val metaResponse = getCorpus(uuid, "viewer1")
        // verify that collaborators does not contain user
        assertEquals(setOf("collab1"), metaResponse.collaborators)
        // and viewers does not contain collab1 or owner
        assertEquals(setOf("viewer1"), metaResponse.viewers)
        assertEquals(owner, metaResponse.owner)
        assertEquals(1, getAllCorpora().size)

        // Update with new collaborators
        val moreSharers = meta.also {
            meta["collaborators"] = mutableSetOf("collab1", "collab2")
            meta["viewers"] = mutableSetOf("viewer1", "viewer2")
        }

        // Try to update as viewer
        assertUpdateCorpusThrows(uuid, moreSharers, "viewer1")
        // Try to update as collaborator
        val moreSharersResponse = updateCorpus(uuid, moreSharers)

        // Check if updated correctly
        assertEquals(setOf("collab1", "collab2"), moreSharersResponse?.collaborators)
        assertEquals(setOf("viewer1", "viewer2"), moreSharersResponse?.viewers)

        // Let viewer2 remove themselves
        val viewer2gone = meta.also {
            meta["collaborators"] = mutableSetOf("collab1", "collab2")
            meta["viewers"] = mutableSetOf("viewer1")
        }
        assertDoesNotThrow { updateCorpus(uuid, viewer2gone, "viewer2") }

        // Try to delete it as viewer1
        assertDeleteCorpusThrows(uuid, "viewer1")
        assertEquals(1, getAllCorpora().size) // Should still be there

        // Delete it as collab1
        assertDeleteCorpusThrows(uuid, "collab1")
        assertEquals(1, getAllCorpora().size) // Should still be there

        // Delete it as the owner
        deleteCorpus(uuid)
        assertEquals(0, getAllCorpora().size) // Gone
    }

    private fun deleteCorpus(uuid: UUID?, user: String = "testUser") {
        mvc.delete("/corpora/$uuid") {
            headers { assignHeaders(this, user) }
        }.andExpect {
            status { isNoContent() }
        }
    }

    private fun assertDeleteCorpusThrows(uuid: UUID?, user: String = "testUser") {
        mvc.delete("/corpora/$uuid") {
            headers { assignHeaders(this, user) }
        }.andExpect {
            status { isForbidden() }
            match { it.resolvedException is CorpusUnauthorizedException }
        }
    }

    private fun postCorpus(body: Any, user: String = TestConfig.TEST_USER): UUID? = mvc.postJson("/corpora", body) {
        headers { assignHeaders(this, user) }
    }.andExpect {
        status { isCreated() }
    }.andReturn().andDeserialize()

    private fun updateCorpus(uuid: UUID?, body: Any, user: String = TestConfig.TEST_USER): CorpusMetadata? =
        mvc.patchJson("/corpora/$uuid", body) {
            headers { assignHeaders(this, user) }
        }.andExpect {
            status { isOk() }
        }.andReturn().andDeserialize()

    private fun assertUpdateCorpusThrows(uuid: UUID?, body: Any, user: String = "testUser") {
        mvc.patchJson("/corpora/$uuid", body) {
            headers { assignHeaders(this, user) }
        }.andExpect {
            status { isForbidden() }
            match { it.resolvedException is CorpusUnauthorizedException }
        }
    }

    private fun getCorpus(uuid: UUID?, user: String = "testUser"): CorpusMetadata = mvc.get("/corpora/$uuid") {
        headers { assignHeaders(this, user) }
    }.andExpect {
        status { isOk() }
    }.andReturn().andDeserialize()

    private fun assertGetCorpusThrows(uuid: UUID?, user: String = "testUser") {
        mvc.get("/corpora/$uuid") {
            headers { assignHeaders(this, user) }
        }.andExpect {
            status { isForbidden() }
            match { it.resolvedException is CorpusUnauthorizedException }
        }
    }

    private fun getAllCorpora(): List<CorpusMetadata> =
        mvc.get("/corpora") { headers(::assignHeaders) }.andExpect { status { isOk() } }.andReturn().andDeserialize()
}