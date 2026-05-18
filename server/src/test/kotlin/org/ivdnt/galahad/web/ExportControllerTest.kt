package org.ivdnt.galahad.web

import java.util.UUID
import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.DocumentNotFoundException
import org.ivdnt.galahad.exceptions.InvalidDocumentFormatException
import org.ivdnt.galahad.exceptions.LayerNotFoundException
import org.ivdnt.galahad.util.*
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get

/** Web controller tests for serialization, status, exception resolving and permissions. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class ExportControllerTest(@Autowired val mvc: MockMvc, @Autowired val config: Config) {

    @Nested
    inner class ConvertDocumentTest {
        private fun canConvertDocument(user: String) {
            val corpus = TestUtil.createFilledCorpus(config)
            val files = TestUtil.get("formats/shared/converter").listFiles()
            val file = files.first { it.name.endsWith(DocumentFormat.TeiP5.extension) }
            val exported =
                performConvertDoc(
                        corpus.uuid,
                        SOURCE_LAYER,
                        file.name,
                        DocumentFormat.Tsv.identifier,
                    )
                    .andExpect {
                        status { isOk() }
                        header { exists("Content-Disposition") }
                        content { contentType(MediaType.TEXT_PLAIN) }
                    }
                    .andReturn()
                    .response
                    .contentAsString
            val expectedFile = files.first { it.name.endsWith(DocumentFormat.Tsv.extension) }
            assertEquals(expectedFile.readText(), exported)
        }

        @Test
        fun `Owner can convert document`() {
            canConvertDocument(TestUtil.TEST_USER)
        }

        @Test
        fun `Admin can convert document`() {
            canConvertDocument("admin")
        }

        @Test
        fun `Collaborator can convert document`() {
            canConvertDocument("collaborator")
        }

        @Test
        fun `Viewer can convert document`() {
            canConvertDocument("viewer")
        }

        @Test fun `Stranger can't convert document`() {}

        @Test
        fun `Can't convert non-existing corpus`() {
            performConvertDoc(
                    UUID.randomUUID(),
                    "non-existing",
                    "non-existing.txt",
                    DocumentFormat.Tsv.identifier,
                )
                .andExpect {
                    status { isNotFound() }
                    match { it.resolvedException is CorpusNotFoundException }
                }
        }

        @Test
        fun `Can't convert non-existing layer`() {
            val corpus = TestUtil.createCorpus(config)
            performConvertDoc(
                    corpus.uuid,
                    "non-existing",
                    "non-existing.txt",
                    DocumentFormat.Tsv.identifier,
                )
                .andExpect {
                    status { isNotFound() }
                    match { it.resolvedException is LayerNotFoundException }
                }
        }

        @Test
        fun `Can't convert non-existing document`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performConvertDoc(
                    corpus.uuid,
                    SOURCE_LAYER,
                    "non-existing.txt",
                    DocumentFormat.Tsv.identifier,
                )
                .andExpect {
                    status { isNotFound() }
                    match { it.resolvedException is DocumentNotFoundException }
                }
        }

        @Test
        fun `Can't convert non-existing format`() {
            val corpus = TestUtil.createFilledCorpus(config)
            val files = TestUtil.get("formats/shared/converter").listFiles()
            val file = files.first()
            performConvertDoc(corpus.uuid, SOURCE_LAYER, file.name, "non-existing").andExpect {
                status { isBadRequest() }
                match { it.resolvedException is InvalidDocumentFormatException }
            }
        }
    }

    private fun performConvertDoc(
        corpus: UUID,
        layer: String = SOURCE_LAYER,
        doc: String,
        format: String,
        user: String = TestUtil.TEST_USER,
    ): ResultActionsDsl =
        mvc.get(
            "/corpora/$corpus/layers/$layer/documents/$doc/export/convert?format={format}",
            format,
        ) {
            headers { assignHeaders(this, user) }
        }
    //
    //    @Test
    //    fun convertAndExportJob() {
    //        val corpus = TestUtil.createFilledCorpus(config)
    //
    //        val uuid = corpus.uuid
    //        val bytes =
    //            mvc.perform(
    //                    MockMvcRequestBuilders.get(
    //                            "/corpora/$uuid/jobs/${TestUtil.TAGGER_NAME}/export/convert"
    //                        )
    //                        .param("format", "folia")
    //                        .headers(::assignHeaders)
    //                )
    //                .andReturn()
    //                .response
    //                .contentAsByteArray
    //        val files = unzip(bytes)
    //        val teiToFolia: File = files.first { it.name.endsWith("tei.folia.xml") }
    //        val result =
    //            TestResult(
    //                TestUtil.get("all-formats/output/from-TeiP5-to-Folia.folia.xml").readText(),
    //                teiToFolia.readText(),
    //            )
    //        result.ignoreLineEndings().ignoreWhiteSpaceDocumentWide().result()
    //    }
    //
    //    private fun unzip(bytes: ByteArray): List<File> {
    //        val zipInputStream = ZipInputStream(bytes.inputStream())
    //        var zipEntry = zipInputStream.nextEntry
    //        val files = mutableListOf<File>()
    //
    //        while (zipEntry != null) {
    //            println("unzipped: " + (zipEntry.name ?: ""))
    //            val file = File.createTempFile("export", zipEntry.name)
    //            file.writeBytes(zipInputStream.readBytes())
    //            files.add(file)
    //            zipEntry = zipInputStream.nextEntry
    //        }
    //
    //        return files
    //    }
    //
    //    @Test fun mergeAndExportJob() {}
    //
    //    //    // Create and populate a corpus with a TEI and Folia document.
    //    //    private fun createAndPopulateCorpus(): Corpus {
    //    //        val corpus = TestUtil.createEmptyCorpus(config)
    //    //        mvc.uploadFile(TestUtil.get("all-formats/input/input.tei.xml"), corpus)
    //    //        // hardcode layer
    //    //        val layer: Layer = LayerBuilder().loadLayerFromTSV(
    //    //            "all-formats/input/pie-tdn.tsv",
    //    //            TestUtil.get("all-formats/input/input.txt").readText()
    //    //        ).build()
    //    //        val job = corpus.jobs.createOrThrow(TestUtil.TAGGER_NAME)
    //    //        job.setLayer("input.tei.xml", layer)
    //    //        //mvc.uploadFile(TestUtil.get("all-formats/input/input.folia.xml"), corpus)
    //    //        return corpus
    //    //    }
}
