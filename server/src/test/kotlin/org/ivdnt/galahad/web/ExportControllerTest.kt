package org.ivdnt.galahad.web

import java.io.File
import java.util.zip.ZipInputStream
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.util.*
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
@Disabled
class ExportControllerTest(@Autowired val mvc: MockMvc, @Autowired val config: Config) {

    @Test
    fun convertAndExportJob() {
        val corpus = TestUtil.createFilledCorpus(config)

        val uuid = corpus.uuid
        val bytes =
            mvc.perform(
                    MockMvcRequestBuilders.get(
                            "/corpora/$uuid/jobs/${TestUtil.TAGGER_NAME}/export/convert"
                        )
                        .param("format", "folia")
                        .headers(::assignHeaders)
                )
                .andReturn()
                .response
                .contentAsByteArray
        val files = unzip(bytes)
        val teiToFolia: File = files.first { it.name.endsWith("tei.folia.xml") }
        val result =
            TestResult(
                TestUtil.get("all-formats/output/from-TeiP5-to-Folia.folia.xml").readText(),
                teiToFolia.readText(),
            )
        result.ignoreLineEndings().ignoreWhiteSpaceDocumentWide().result()
    }

    private fun unzip(bytes: ByteArray): List<File> {
        val zipInputStream = ZipInputStream(bytes.inputStream())
        var zipEntry = zipInputStream.nextEntry
        val files = mutableListOf<File>()

        while (zipEntry != null) {
            println("unzipped: " + (zipEntry.name ?: ""))
            val file = File.createTempFile("export", zipEntry.name)
            file.writeBytes(zipInputStream.readBytes())
            files.add(file)
            zipEntry = zipInputStream.nextEntry
        }

        return files
    }

    @Test fun mergeAndExportJob() {}

    //    // Create and populate a corpus with a TEI and Folia document.
    //    private fun createAndPopulateCorpus(): Corpus {
    //        val corpus = TestUtil.createEmptyCorpus(config)
    //        mvc.uploadFile(TestUtil.get("all-formats/input/input.tei.xml"), corpus)
    //        // hardcode layer
    //        val layer: Layer = LayerBuilder().loadLayerFromTSV(
    //            "all-formats/input/pie-tdn.tsv",
    //            TestUtil.get("all-formats/input/input.txt").readText()
    //        ).build()
    //        val job = corpus.jobs.createOrThrow(TestUtil.TAGGER_NAME)
    //        job.setLayer("input.tei.xml", layer)
    //        //mvc.uploadFile(TestUtil.get("all-formats/input/input.folia.xml"), corpus)
    //        return corpus
    //    }
}
