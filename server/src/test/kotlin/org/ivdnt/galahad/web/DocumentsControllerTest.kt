package org.ivdnt.galahad.web

import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.DocumentMetadata
import org.ivdnt.galahad.exceptions.CorpusUnauthorizedException
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.exceptions.DocumentNotFoundException
import org.ivdnt.galahad.util.*
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import java.io.File
import java.util.*

@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class DocumentsControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val config: Config,
) {

    @Test
    fun `download non-existing doc`() {
        val corpus = TestUtil.createEmptyCorpus(config)
        mvc.get("/corpora/${corpus.uuid}/documents/nonexisting/download") {
            headers(::assignHeaders)
        }.andExpect {
            status { isNotFound() }
            match { it.resolvedException is DocumentNotFoundException }
        }
    }

    @Test
    fun `Upload files of all formats`() {
        val corpus = TestUtil.createEmptyCorpus(config)
        // list files in directory
        val dir: File = TestUtil.get("formats/shared/converter")
        val files = dir.listFiles()
        files.forEach { mvc.uploadFile(it, corpus) }
        // check if all files are uploaded
        assertEquals(files.size, getDocs(corpus).size)
    }

    @Test
    fun `Get raw files`() {
        val corpus = TestUtil.createFilledCorpus(config)
        val docs = getDocs(corpus)
        val uuid = corpus.uuid
        for (doc in docs) {
            val result =
                mvc.get("/corpora/$uuid/documents/${doc.name}/download") { headers(::assignHeaders) }.andReturn()
            assertEquals(
                TestUtil.get("formats/shared/converter/${doc.name}").readText(), result.response.contentAsString
            )
        }
    }

    @Test
    fun `Test delete permissions`() {
        val corpus = TestUtil.createFilledCorpus(config)
        val uuid = corpus.uuid
        val files = TestUtil.get("formats/shared/converter").listFiles()
        val file = files.first()
        // try to delete as stranger
        assertDeleteDocThrows(uuid, file, "stranger")
        // try to delete as viewer
        assertDeleteDocThrows(uuid, file, "viewer")
        // same number of files
        assertEquals(files.size, getDocs(corpus).size)
        // Delete as collaborator
        mvc.delete("/corpora/$uuid/documents/${file.name}") { headers { assignHeaders(this, "collaborator") } }
            .andExpect {
                status { isNoContent() }
            }
        assertEquals(files.size - 1, getDocs(corpus).size)
    }

    private fun assertDeleteDocThrows(uuid: UUID, file: File, username: String) {
        mvc.delete("/corpora/$uuid/documents/${file.name}") { headers { assignHeaders(this, username) } }.andExpect {
            status { isForbidden() }
            match { it.resolvedException is CorpusUnauthorizedException }
        }
    }

    @Test
    fun `Upload zip with all formats`() {
        val corpus = TestUtil.createEmptyCorpus(config)
        val dir = TestUtil.get("formats/shared/converter")
        val files = dir.listFiles()
        val zip = zipDir(dir)
        mvc.uploadFile(zip, corpus, MediaType.APPLICATION_OCTET_STREAM_VALUE)
        assertEquals(files.size, getDocs(corpus).size)
    }

    @Test
    fun `Upload invalid xml file`() {
        val corpus = TestUtil.createEmptyCorpus(config)
        val file = TestUtil.get("formats/invalid/invalid.xml")
        val res = mvc.uploadFile(file, corpus, MediaType.APPLICATION_XML_VALUE)
        assertEquals(res.resolvedException!!::class, DocumentInvalidException::class)
    }

    @Test
    fun `Upload invalid other extension`() {
        val corpus = TestUtil.createEmptyCorpus(config)
        val file = TestUtil.get("formats/invalid/invalid.mp3")
        val res = mvc.uploadFile(file, corpus, MediaType.ALL_VALUE)
        assertEquals(res.resolvedException!!::class, DocumentInvalidException::class)
    }

    private fun getDocs(corpus: Corpus): List<DocumentMetadata> =
        mvc.get("/corpora/${corpus.uuid}/documents") {
            headers(::assignHeaders)
        }.andReturn().andDeserialize()
}