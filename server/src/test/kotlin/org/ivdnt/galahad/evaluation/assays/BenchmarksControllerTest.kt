package org.ivdnt.galahad.evaluation.assays

//
//@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
//@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
//class BenchmarksControllerTest(
//    @Autowired val mvc: MockMvc,
//    @Autowired val config: Config,
//    @Autowired val ctrl: BenchmarksController,
//) {
//    @Test
//    fun getAssays() {
//        // No assays should exist
//        var assays = ctrl.benchmarksMatrix.readOrCreate<BenchmarksMatrix>()
//        assertEquals(0, assays.size)
//
//        // Need a corpus first
//        val corpus = TestUtil.createCorpus(
//            config.getWorkingDirectory().resolve("corpora").resolve("custom"),
//            isDataset = true,
//            isAdmin = true
//        )
//
//        // Neither should individual ones
//        val assayRequest: MvcResult = mvc.perform(
//            MockMvcRequestBuilders.get("/benchmarks/${corpus.immutableMetadata.uuid}/pie-tdn")
//        ).andReturn()
//        assertEquals("", assayRequest.response.contentAsString)
//
//        // Add result
//        val doc = corpus.documents.createOrThrow(File.createTempFile("tmp", ".txt"))
//        val layer = LayerBuilder().loadDummies(100).build()
//        EvaluationUtil.addLayersAsJobs(corpus, doc.name, layer, layer)
//
//        // job assay should exist
//        assertNotNull(corpus.jobs.readOrThrow(TestConfig.TAGGER_NAME).assay.readOrCreate<Map<String, FlatMetricType>>())
//
//        // /GET
//        val assaysRequest: MvcResult = mvc.perform(
//            MockMvcRequestBuilders.get("/benchmarks")
//        ).andReturn()
//        assays = JSON.fromStr<BenchmarksMatrix>(assaysRequest.response.contentAsString)
//        assertEquals(1, assays.size)
//        assertEquals(1f, assays["testCorpus"]!!["lemmaPosByPos"]!![TestConfig.TAGGER_NAME]!!.micro.accuracy, 0.00001f)
//        // We don't want the source layer, as it would always 100% agree with itself.
//        assertFalse(assays.containsKey("sourceLayer"))
//    }
//}