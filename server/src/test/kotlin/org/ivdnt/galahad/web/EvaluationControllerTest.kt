package org.ivdnt.galahad.web

// TODO check content disposition header

//
// import java.io.File
// import java.nio.charset.StandardCharsets
// import java.util.zip.ZipInputStream
// import org.ivdnt.galahad.app.Config
// import org.ivdnt.galahad.app.Galahad
// import org.ivdnt.galahad.corpora.Corpus
// import org.ivdnt.galahad.evaluation.EvaluationUtil
// import org.ivdnt.galahad.util.TestConfig
// import org.ivdnt.galahad.util.TestUtil
// import org.ivdnt.galahad.util.addUrlParams
// import org.junit.jupiter.api.Assertions
// import org.junit.jupiter.api.Disabled
// import org.junit.jupiter.api.Test
// import org.junit.jupiter.api.fail
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
// import org.springframework.test.context.ContextConfiguration
// import org.springframework.test.web.servlet.MockMvc
// import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
//
// @WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
// @ContextConfiguration(classes = [Galahad::class, TestConfig::class])
// @Disabled
// class EvaluationControllerTest(@Autowired val mvc: MockMvc, @Autowired val config: Config) {
//    @Test
//    fun `Download evaluation zip of all 3`() {
//        val corpus = corpus()
//        EvaluationUtil.addDocWithMatchingMultiPosLemma(corpus)
//
//        // url
//        val uuid = corpus.uuid
//        val url =
//
// "/corpora/$uuid/jobs/${TestUtil.TAGGER_NAME}/evaluation/download?reference=sourceLayer"
//        // /GET
//        val bytes =
//            mvc.perform(MockMvcRequestBuilders.get(url).headers(TestUtil::assignHeaders))
//                .andReturn()
//                .response
//                .contentAsByteArray
//        assertEvalZip(bytes)
//    }
//
//    @Test
//    fun `Download confusion samples`() {
//        val corpus = corpus()
//        // url
//        val uuid = corpus.uuid
//        val params =
//            mapOf(
//                "reference" to "sourceLayer",
//                "annotation" to "pos",
//                "refFilter" to "ADJ",
//                "hypoFilter" to "ADJ",
//            )
//        val url =
//            "/corpora/$uuid/jobs/${TestUtil.TAGGER_NAME}/evaluation/confusion/download"
//                .addUrlParams(params)
//        val bytes =
//            mvc.perform(MockMvcRequestBuilders.get(url).headers(TestUtil::assignHeaders))
//                .andReturn()
//                .response
//                .contentAsByteArray
//        assertSingleFileZip(bytes, "confusion-samples.csv")
//    }
//
//    @Test
//    fun `Download metrics samples`() {
//        val corpus = corpus()
//        // url
//        val uuid = corpus.uuid
//        val params =
//            mapOf(
//                "reference" to "sourceLayer",
//                "metricsType" to "posByPos",
//                "class" to "truePositive",
//                "group" to "ADJ",
//            )
//        val url =
//            "/corpora/$uuid/jobs/${TestUtil.TAGGER_NAME}/evaluation/metrics/download"
//                .addUrlParams(params)
//        val bytes =
//            mvc.perform(MockMvcRequestBuilders.get(url).headers(TestUtil::assignHeaders))
//                .andReturn()
//                .response
//                .contentAsByteArray
//        assertSingleFileZip(bytes, "metrics-samples.csv")
//    }
//
//    private fun corpus(): Corpus {
//        // Need a corpus first
//        val corpus = TestUtil.createCorpus(config)
//        EvaluationUtil.add_two_docs_to_corpus(corpus)
//        EvaluationUtil.addDocWithMissingMatches(corpus)
//        return corpus
//    }
//
//    private fun assertEvalZip(byteArray: ByteArray) {
//        val zipInputStream = ZipInputStream(byteArray.inputStream())
//        var zipEntry = zipInputStream.nextEntry
//        var evals = 0
//        while (zipEntry != null) {
//            println("unzipped: " + (zipEntry.name ?: ""))
//            val fileContent = String(zipInputStream.readAllBytes(), StandardCharsets.UTF_16LE)
//            if (zipEntry.name.split(".")[1] == "csv") {
//                val f: File
//                try {
//                    f = TestUtil.get("evaluation/zip/${zipEntry.name}")
//                    val content: String = f.readText()
//                    Assertions.assertEquals(content, fileContent)
//                    evals++
//                } catch (e: Exception) {
//                    println("File not found: ${zipEntry.name}")
//                    fail(e)
//                }
//            }
//            zipEntry = zipInputStream.nextEntry
//        }
//        Assertions.assertEquals(13, evals)
//    }
//
//    private fun assertSingleFileZip(byteArray: ByteArray, expected: String) {
//        val zipInputStream = ZipInputStream(byteArray.inputStream())
//        var zipEntry = zipInputStream.nextEntry
//        var evals = 0
//        while (zipEntry != null) {
//            println("unzipped: " + (zipEntry.name ?: ""))
//            val fileContent = String(zipInputStream.readAllBytes(), StandardCharsets.UTF_16LE)
//            if (zipEntry.name.split(".")[1] == "csv") {
//                Assertions.assertEquals(
//                    TestUtil.get("evaluation/zip/$expected").readText(),
//                    fileContent,
//                )
//                evals++
//            }
//            zipEntry = zipInputStream.nextEntry
//        }
//        Assertions.assertEquals(1, evals)
//    }
// }
