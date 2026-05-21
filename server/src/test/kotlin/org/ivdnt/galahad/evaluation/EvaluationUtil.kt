package org.ivdnt.galahad.evaluation
//
//import java.io.File
//import org.ivdnt.galahad.annotations.Layer
//import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER
//import org.ivdnt.galahad.corpora.Corpus
//import org.ivdnt.galahad.util.LayerBuilder
//import org.ivdnt.galahad.util.TestUtil
//
//object EvaluationUtil {
//    /**
//     * Add two documents to the corpus, each with two layers, one source, one pie-tdn. The first doc
//     * has equivalent layers, the second has different layers (i.e. with mistakes).
//     *
//     * First doc: 1 NOU, 2 VRB (in both layers) Second doc:
//     * - 3 ADJ, 4 PD (in hypothesis/pie-tdn),
//     * - 2 ADJ correct, 1 ADJ WRONG lemma, 3 PD correct, 1 PD WRONG pos (in reference/source)
//     */
//    fun add_two_docs_to_corpus(corpus: Corpus) {
//        // Create the first doc
//        val tmpFile = File.createTempFile("tmp", ".txt")
//        tmpFile.writeText(
//            "dummy dummy dummy."
//        ) // write some text for alphabetic distribution coverage
//        val doc1 = corpus.documents.createOrThrow(tmpFile)
//        // Create two layers. We'll use the same.
//        val builder1 = LayerBuilder().loadDummies(1, pos = "NOU").loadDummies(2, pos = "VRB")
//        val hypoLayer1 = builder1.build()
//        val refLayer1 = builder1.build()
//        // Add the layers as jobs
//        addLayersAsJobs(corpus, doc1.name, hypoLayer1, refLayer1)
//
//        // Create the second doc
//        val doc2 = corpus.documents.createOrThrow(File.createTempFile("tmp", ".txt"))
//        // Create two layers. This time, we'll use different ones.
//        val refLayer2 =
//            LayerBuilder().loadDummies(3, pos = "ADJ").loadDummies(4, pos = "PD").build()
//        val hypoLayer2 =
//            LayerBuilder()
//                // same as above, but the third lemma is wrong
//                .loadDummies(2, pos = "ADJ")
//                .loadDummies(1, lemma = "WRONG", pos = "ADJ")
//                // same as above, but the fourth pos is wrong
//                .loadDummies(3, pos = "PD")
//                .loadDummies(1, pos = "WRONG")
//                .build()
//        // Add the layers as jobs
//        addLayersAsJobs(corpus, doc2.name, hypoLayer2, refLayer2)
//    }
//
//    /**
//     * Missing match due to punctuation. Hypothesis: 5 terms: "dummy," "dummy" "." "." "."
//     * Reference: 4 terms: "dummy" "," "dummy" "..."
//     */
//    fun addDocWithMissingMatches(corpus: Corpus) {
//        val doc = corpus.documents.createOrThrow(File.createTempFile("tmp", ".txt"))
//        val hypo =
//            LayerBuilder()
//                .loadDummies(1, pos = "VRB")
//                .loadDummies(1, ",", pos = "LET")
//                .loadDummies(1, pos = "ADV")
//                .loadDummies(3, ".", pos = "LET")
//                .build()
//        val ref =
//            LayerBuilder()
//                .loadDummies(1, "dummy,", pos = "ADV")
//                .loadDummies(1, pos = "ADV")
//                .loadDummies(1, "...", pos = "LET")
//                .build()
//        addLayersAsJobs(corpus, doc.name, hypo, ref)
//    }
//
//    fun addDocWithMatchingMultiPosLemma(corpus: Corpus) {
//        val doc = corpus.documents.createOrThrow(File.createTempFile("tmp", ".txt"))
//        val layer =
//            LayerBuilder().loadDummies(1, "tboek", pos = "PD+NOU-C", lemma = "het+boek").build()
//        addLayersAsJobs(corpus, doc.name, layer, layer)
//    }
//
//    private fun addLayersAsJobs(corpus: Corpus, docName: String, tagger: Layer, source: Layer) {
//        val taggerJob = corpus.jobs.createOrThrow(TestUtil.TAGGER_NAME)
//        taggerJob.setLayer(docName, tagger)
//        val sourceJob = corpus.jobs.createOrThrow(SOURCE_LAYER)
//        sourceJob.setLayer(docName, source)
//    }
//}
