package org.ivdnt.galahad.formats.naf

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.DocTest
import org.ivdnt.galahad.formats.LayerBuilder
import org.ivdnt.galahad.formats.Resource
import org.ivdnt.galahad.formats.createCorpus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempDirectory

class NafExportTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Convert dummy layer to Naf`() {
        val NUM_DUMMIES = 3
        val layer = LayerBuilder().loadDummies(NUM_DUMMIES).build()
        val plainFile = createTempDirectory().toFile().resolve("dummy.txt")
        plainFile.writer().use { writer ->
            writer.write((0 until NUM_DUMMIES).map { "dummy" }.joinToString(" "))
        }
        DocTest.builder(corpus)
            .expectingFile("naf/export/converted-output.naf.xml")
            // document.parse() will be called and throw on an empty file, hence the dummy file.
            .convertToNaf(plainFile, layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }

    @Test
    fun `Merge throws`() {
        val layer = LayerBuilder().loadDummies(1).build()
        val meta = DocTest.builder(corpus).getDummyTransformMetadata(layer, DocumentFormat.Txt)
        assertThrows(Exception::class.java) {
            Resource.getDoc("naf/import/input.naf.xml").merge(meta)
        }
    }
}