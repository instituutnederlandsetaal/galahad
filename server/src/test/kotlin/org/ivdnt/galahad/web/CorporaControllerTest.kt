package org.ivdnt.galahad.web

import java.util.*
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.corpora.CorpusStatistics
import org.ivdnt.galahad.exceptions.CorpusInvalidException
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.CorpusUnauthorizedException
import org.ivdnt.galahad.util.*
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import tools.jackson.module.kotlin.convertValue

/** Web controller tests for serialization, status, exception resolving and permissions. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class CorporaControllerTest(@Autowired val mvc: MockMvc) {
    @BeforeEach
    fun setUp() {
        TestConfig.reset()
    }

    @Nested
    inner class CorpusCreationTest {
        @Test
        fun `Can create corpus`() {
            val uuid = postCorpus(mapOf("name" to "test"))
            getCorpus(uuid)
            assertEquals(1, getAllCorpora().size)
        }

        @Test
        fun `Can create unicode name corpus`() {
            val name = "日本語"
            val uuid = postCorpus(mapOf("name" to name))
            val corpus = getCorpus(uuid)
            assertEquals(name, corpus.name)
            assertEquals(1, getAllCorpora().size)
        }

        @Test
        fun `Can create corpus with metadata`() {
            val body =
                mapOf(
                    "owner" to TestUtil.TEST_USER,
                    "name" to "test",
                    "language" to "Dutch",
                    "tagset" to "TDN-Core",
                    "viewers" to setOf("viewer"),
                    "collaborators" to setOf("collaborator"),
                )
            val uuid = postCorpus(body)
            val corpus = getCorpus(uuid)
            assertEquals(1, getAllCorpora().size)
            // check for each value in body that it exists and is correct on corpus
            // need to convert corpus to a json map first
            val corpusMap = JsonUtil.mapper.convertValue<Map<String, Any>>(corpus)
            for (key in body.keys) {
                assertEquals(body[key].toString(), corpusMap[key].toString())
            }
        }

        @Test
        fun `Admin can create dataset corpus`() {
            val uuid = postCorpus(mapOf("name" to "test", "dataset" to true), "admin")
            val corpus = getCorpus(uuid)
            assertEquals(true, corpus.dataset)
            assertEquals(1, getAllCorpora().size)
        }

        @Test
        fun `Corpus creation handles duplicate users of different permission levels`() {
            val body =
                mapOf(
                    "name" to "test",
                    "viewers" to setOf("viewer", "collaborator", TestUtil.TEST_USER),
                    "collaborators" to setOf("collaborator", TestUtil.TEST_USER),
                )
            val uuid = postCorpus(body)
            val corpus = getCorpus(uuid)
            assertEquals(setOf("viewer"), corpus.viewers)
            assertEquals(setOf("collaborator"), corpus.collaborators)
        }

        @Test
        fun `Can't create corpus with blank name`() {
            mvc.postJson("/corpora", mapOf("name" to "   ")) { headers(::assignHeaders) }
                .andExpect {
                    status { isBadRequest() }
                    match { it.resolvedException is CorpusInvalidException }
                }
            assertEquals(0, getAllCorpora().size)
        }

        @Test
        fun `Non-admin can't create database corpus`() {
            mvc.postJson("/corpora", mapOf("name" to "test", "dataset" to true)) {
                    headers { assignHeaders(this, "non-admin") }
                }
                .andExpect {
                    status { isForbidden() }
                    match { it.resolvedException is CorpusUnauthorizedException }
                }
            assertEquals(0, getAllCorpora().size)
        }
    }

    @Nested
    inner class CorpusGetTest {
        @Test
        fun `Viewer can access corpus`() {
            val uuid = postCorpus(mapOf("name" to "test", "viewers" to setOf("viewer")))
            getCorpus(uuid, "viewer")
        }

        @Test
        fun `Collaborator can access corpus`() {
            val uuid = postCorpus(mapOf("name" to "test", "collaborators" to setOf("collaborator")))
            getCorpus(uuid, "collaborator")
        }

        @Test
        fun `Admin can access corpus`() {
            val uuid = postCorpus(mapOf("name" to "test"))
            getCorpus(uuid, "admin")
        }

        @Test
        fun `Stranger can access dataset corpus`() {
            val uuid = postCorpus(mapOf("name" to "test", "dataset" to true), "admin")
            getCorpus(uuid, "stranger")
        }

        @Test
        fun `Can't get non-existing corpus`() {
            mvc.get("/corpora/${UUID.randomUUID()}").andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }

        @Test
        fun `Getting non-uuid corpus is bad request`() {
            mvc.get("/corpora/non-uuid").andExpect { status { isBadRequest() } }
        }

        @Test
        fun `Stranger can't get corpus`() {
            val uuid = postCorpus(mapOf("name" to "test"))
            performGetCorpus(uuid, "stranger").andExpect {
                status { isForbidden() }
                match { it.resolvedException is CorpusUnauthorizedException }
            }
        }
    }

    @Nested
    inner class CorpusUpdateTest {
        @Test
        fun `Owner can update corpus`() {
            val uuid = postCorpus(mapOf("name" to "original"))
            val updated = updateCorpus(uuid, mapOf("name" to "updated"))
            assertEquals("updated", updated.name)
        }

        @Test
        fun `Admin can update corpus to dataset`() {
            val uuid = postCorpus(mapOf("name" to "test"))
            val updated = updateCorpus(uuid, mapOf("name" to "test", "dataset" to true), "admin")
            assertEquals(true, updated.dataset)
        }

        @Test
        fun `Collaborator can update corpus`() {
            val uuid = postCorpus(mapOf("name" to "original"))
            val updated = updateCorpus(uuid, mapOf("name" to "updated"))
            assertEquals("updated", updated.name)
        }

        @Test
        fun `Can't update non-existing corpus`() {
            performUpdateCorpus(UUID.randomUUID(), mapOf("name" to "updated")).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }

        @Test
        fun `Stranger can't update corpus`() {
            val uuid = postCorpus(mapOf("name" to "original"))
            performUpdateCorpus(uuid, mapOf("name" to "updated"), "stranger").andExpect {
                status { isForbidden() }
                match { it.resolvedException is CorpusUnauthorizedException }
            }
            val corpus = getCorpus(uuid)
            assertEquals("original", corpus.name)
        }

        @Test
        fun `Viewer can't update corpus`() {
            val uuid = postCorpus(mapOf("name" to "original", "viewers" to setOf("viewer")))
            val updated = mapOf("name" to "updated", "viewers" to setOf("viewer"))
            performUpdateCorpus(uuid, updated, "viewer").andExpect {
                status { isForbidden() }
                match { it.resolvedException is CorpusUnauthorizedException }
            }
            val corpus = getCorpus(uuid)
            assertEquals("original", corpus.name)
        }

        @Test
        fun `Viewer can remove self from corpus`() {
            val uuid = postCorpus(mapOf("name" to "test", "viewers" to setOf("viewer")))
            val updated =
                updateCorpus(
                    uuid,
                    mapOf("name" to "test", "viewers" to emptySet<String>()),
                    "viewer",
                )
            assertEquals(emptySet<String>(), updated.viewers)
        }

        @Test
        fun `Collaborator can't remove others`() {
            val uuid =
                postCorpus(
                    mapOf(
                        "name" to "test",
                        "collaborators" to setOf("collaborator1", "collaborator2"),
                    )
                )
            performUpdateCorpus(
                    uuid,
                    mapOf("name" to "test", "collaborators" to setOf("collaborator1")),
                    "collaborator1",
                )
                .andExpect {
                    status { isForbidden() }
                    match { it.resolvedException is CorpusUnauthorizedException }
                }
            val corpus = getCorpus(uuid)
            assertEquals(setOf("collaborator1", "collaborator2"), corpus.collaborators)
        }

        @Test
        fun `Collaborator can remove self from corpus`() {
            val uuid = postCorpus(mapOf("name" to "test", "collaborators" to setOf("collaborator")))
            val updated =
                updateCorpus(
                    uuid,
                    mapOf("name" to "test", "collaborators" to emptySet<String>()),
                    "collaborator",
                )
            assertEquals(emptySet<String>(), updated.collaborators)
        }
    }

    @Nested
    inner class CorpusDeletionTest {
        @Test
        fun `Owner can delete corpus`() {
            val uuid = postCorpus(mapOf("name" to "test"))
            assertEquals(1, getAllCorpora().size)
            deleteCorpus(uuid)
            performGetCorpus(uuid).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
            assertEquals(0, getAllCorpora().size)
        }

        @Test
        fun `Admin can delete corpus`() {
            val uuid = postCorpus(mapOf("name" to "test"))
            assertEquals(1, getAllCorpora().size)
            deleteCorpus(uuid, "admin")
            performGetCorpus(uuid).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
            assertEquals(0, getAllCorpora().size)
        }

        @Test
        fun `Can't delete non-existing corpus`() {
            performDeleteCorpus(UUID.randomUUID()).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }

        @Test
        fun `Stranger can't delete corpus`() {
            val corpus = postCorpus(mapOf("name" to "test"))
            assertEquals(1, getAllCorpora().size)
            assertDeleteCorpusForbidden(corpus, "stranger")
            assertEquals(1, getAllCorpora().size)
        }

        @Test
        fun `Stranger can't delete database corpus`() {
            val corpus = postCorpus(mapOf("name" to "test", "dataset" to true), "admin")
            assertEquals(1, getAllCorpora().size)
            assertDeleteCorpusForbidden(corpus, "stranger")
            assertEquals(1, getAllCorpora().size)
        }

        @Test
        fun `Viewer can't delete corpus`() {
            val corpus = postCorpus(mapOf("name" to "test", "viewers" to setOf("viewer")))
            assertEquals(1, getAllCorpora().size)
            assertDeleteCorpusForbidden(corpus, "viewer")
            assertEquals(1, getAllCorpora().size)
        }

        @Test
        fun `Collaborator can't delete corpus`() {
            val corpus =
                postCorpus(mapOf("name" to "test", "collaborators" to setOf("collaborator")))
            assertEquals(1, getAllCorpora().size)
            assertDeleteCorpusForbidden(corpus, "collaborator")
            assertEquals(1, getAllCorpora().size)
        }
    }

    private fun performPostCorpus(body: Any, user: String = TestUtil.TEST_USER): ResultActionsDsl =
        mvc.postJson("/corpora", body) { headers { assignHeaders(this, user) } }

    private fun postCorpus(body: Any, user: String = TestUtil.TEST_USER): UUID =
        performPostCorpus(body, user)
            .andExpect { status { isCreated() } }
            .andReturn()
            .andDeserialize()

    private fun performGetCorpus(uuid: UUID?, user: String = TestUtil.TEST_USER): ResultActionsDsl =
        mvc.get("/corpora/$uuid") { headers { assignHeaders(this, user) } }

    private fun getCorpus(uuid: UUID?, user: String = TestUtil.TEST_USER): CorpusStatistics =
        performGetCorpus(uuid, user).andExpect { status { isOk() } }.andReturn().andDeserialize()

    private fun getAllCorpora(): List<CorpusStatistics> =
        mvc.get("/corpora") { headers(::assignHeaders) }
            .andExpect { status { isOk() } }
            .andReturn()
            .andDeserialize()

    private fun performDeleteCorpus(
        uuid: UUID?,
        user: String = TestUtil.TEST_USER,
    ): ResultActionsDsl = mvc.delete("/corpora/$uuid") { headers { assignHeaders(this, user) } }

    private fun deleteCorpus(uuid: UUID?, user: String = TestUtil.TEST_USER) {
        performDeleteCorpus(uuid, user).andExpect { status { isNoContent() } }
    }

    private fun assertDeleteCorpusForbidden(uuid: UUID?, user: String = TestUtil.TEST_USER) {
        performDeleteCorpus(uuid, user).andExpect {
            status { isForbidden() }
            match { it.resolvedException is CorpusUnauthorizedException }
        }
    }

    private fun performUpdateCorpus(
        uuid: UUID?,
        body: Any,
        user: String = TestUtil.TEST_USER,
    ): ResultActionsDsl =
        mvc.patchJson("/corpora/$uuid", body) { headers { assignHeaders(this, user) } }

    private fun updateCorpus(
        uuid: UUID?,
        body: Any,
        user: String = TestUtil.TEST_USER,
    ): CorpusStatistics =
        performUpdateCorpus(uuid, body, user)
            .andExpect { status { isOk() } }
            .andReturn()
            .andDeserialize()
}
