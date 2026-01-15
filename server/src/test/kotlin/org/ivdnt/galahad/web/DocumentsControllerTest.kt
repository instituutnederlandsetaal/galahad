package org.ivdnt.galahad.web

import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.DocumentMetadata
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.util.JSON
import org.ivdnt.galahad.util.SpringUtil
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.util.UserHeader
import org.ivdnt.galahad.util.uploadFile
import org.ivdnt.galahad.web.controller.DocumentsController
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class DocumentsControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val config: Config,
    @Autowired val ctrl: DocumentsController,
) {

    @Test
    fun `Upload files of all formats`() {
        val corpus = SpringUtil.createCorpus(config)

        // list files in directory
        val dir: File = TestUtil.get("formats/shared/converter")
        for (file in dir.listFiles()) {
            // skip layer pie-tdn.tsv
            if (file.name != "pie-tdn.tsv") {
                mvc.uploadFile(file, corpus)
            }
        }
        // check if all files are uploaded
        assertEquals(6, getDocs(corpus).size)
        // Get raw file
        val doc = getDocs(corpus)[0]
        val uuid = corpus.immutableMetadata.uuid
        val result: MvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/corpora/$uuid/documents/${doc.name}/download").headers(UserHeader.get())
        ).andReturn()
        assertEquals(TestUtil.get("formats/shared/converter/${doc.name}").readText(), result.response.contentAsString)
        // Delete a doc
        mvc.perform(
            MockMvcRequestBuilders.delete("/corpora/$uuid/documents/${doc.name}").headers(UserHeader.get())
        )

        assertEquals(5, getDocs(corpus).size)
    }

    @Test
    fun `Upload zip with all formats`() {
        val corpus = SpringUtil.createCorpus(config)
        val files = TestUtil.get("formats/shared/converter").listFiles()
        val zip = zipped(files.asIterable())
        mvc.uploadFile(zip, corpus, MediaType.APPLICATION_OCTET_STREAM_VALUE)
        assertEquals(6, getDocs(corpus).size)
    }

    private fun zipped(files: Iterable<File>): File {
        val zipFile = File.createTempFile("tmp", ".zip")
        val zipStream = ZipOutputStream(FileOutputStream(zipFile))
        for (f in files) {
            zipStream.putNextEntry(ZipEntry(f.name))
            f.inputStream().copyTo(zipStream)
            zipStream.closeEntry()
        }
        zipStream.flush()
        zipStream.close()
        return zipFile
    }

    @Test
    fun `Upload invalid file`() {
        val corpus = SpringUtil.createCorpus(config)
        val file = TestUtil.get("documents/invalid-root.xml")
        val res = mvc.uploadFile(file, corpus, MediaType.APPLICATION_XML_VALUE)
        assertEquals(res.resolvedException!!::class, DocumentInvalidException::class)
    }

    private fun getDocs(corpus: Corpus): List<DocumentMetadata> {
        // Request doc metadata
        val uuid = corpus.immutableMetadata.uuid
        val result: MvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/corpora/$uuid/documents").headers(UserHeader.get())
        ).andReturn()

        // check doc count
        val docs: List<DocumentMetadata> = JSON.fromStr(result.response.contentAsString)
        return docs
    }


}