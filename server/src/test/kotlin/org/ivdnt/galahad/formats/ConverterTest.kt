package org.ivdnt.galahad.formats

import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.corpora.documents.Documents
import org.ivdnt.galahad.export.CorpusExport
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayOutputStream
import java.io.File
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

open class ConverterTest {
    lateinit var corpus: Corpus
    val documents: Documents
        get() = corpus.documents
    val fileName = "input"

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    fun formatToFormat(input: DocumentFormat, output: DocumentFormat) {
        val inputFile: File = Resource.get("docs-pars-sents/$fileName.${input.extension}")
        val outputFile: File = Resource.get("docs-pars-sents/$fileName.${output.extension}")
        val doc = documents.createOrThrow(inputFile)
        // at this point, a layer id has been assigned
        // but some formats don't have a layer id (i.e. file level id)
        // so we overwrite the layer id for those formats
        if (input in arrayOf(DocumentFormat.Tsv, DocumentFormat.Conllu, DocumentFormat.Txt)) {
            // retrieve their source layer
            val layer = corpus.jobs.createOrThrow(SOURCE_LAYER_NAME).getLayer(doc)
            val fixedLayer = Layer(layer.documents, "galahadTest")
            corpus.jobs.createOrThrow(SOURCE_LAYER_NAME).setLayer(doc, fixedLayer)
        }

        val corpusExport = CorpusExport.create(corpus, SOURCE_LAYER_NAME, output, false, User.DEFAULT_USER, false)
        val docExport = corpusExport.documentExport(doc)
        val converted = ByteArrayOutputStream().also { docExport.convert(it); it.flush() }.toString()
        assertEquals(outputFile.readText(), converted)
    }

    @Nested
    inner class Tei {
        @Test
        fun `Tei to Tei`() {
            formatToFormat(DocumentFormat.TeiP5, DocumentFormat.TeiP5)
        }

        @Test
        fun `Tei to Folia`() {
            formatToFormat(DocumentFormat.TeiP5, DocumentFormat.Folia)
        }

        @Test
        fun `Tei to Tsv`() {
            formatToFormat(DocumentFormat.TeiP5, DocumentFormat.Tsv)
        }

        @Test
        fun `Tei to TXT`() {
            formatToFormat(DocumentFormat.TeiP5, DocumentFormat.Txt)
        }

        @Test
        fun `Tei to Conllu`() {
            formatToFormat(DocumentFormat.TeiP5, DocumentFormat.Conllu)
        }
    }

    @Nested
    inner class Folia {
        @Test
        fun `Folia to Folia`() {
            formatToFormat(DocumentFormat.Folia, DocumentFormat.Folia)
        }

        @Test
        fun `Folia to Tei`() {
            formatToFormat(DocumentFormat.Folia, DocumentFormat.TeiP5)
        }

        @Test
        fun `Folia to Tsv`() {
            formatToFormat(DocumentFormat.Folia, DocumentFormat.Tsv)
        }

        @Test
        fun `Folia to TXT`() {
            formatToFormat(DocumentFormat.Folia, DocumentFormat.Txt)
        }

        @Test
        fun `Folia to Conllu`() {
            formatToFormat(DocumentFormat.Folia, DocumentFormat.Conllu)
        }
    }

    @Nested
    inner class Tsv {
        @Test
        fun `Tsv to Tsv`() {
            formatToFormat(DocumentFormat.Tsv, DocumentFormat.Tsv)
        }

        @Test
        fun `Tsv to Tei`() {
            formatToFormat(DocumentFormat.Tsv, DocumentFormat.TeiP5)
        }

        @Test
        fun `Tsv to Folia`() {
            formatToFormat(DocumentFormat.Tsv, DocumentFormat.Folia)
        }

        @Test
        fun `Tsv to TXT`() {
            formatToFormat(DocumentFormat.Tsv, DocumentFormat.Txt)
        }

        @Test
        fun `Tsv to Conllu`() {
            formatToFormat(DocumentFormat.Tsv, DocumentFormat.Conllu)
        }
    }

    @Nested
    inner class Conllu {
        @Test
        fun `Conllu to Conllu`() {
            formatToFormat(DocumentFormat.Conllu, DocumentFormat.Conllu)
        }

        @Test
        fun `Conllu to Tei`() {
            formatToFormat(DocumentFormat.Conllu, DocumentFormat.TeiP5)
        }

        @Test
        fun `Conllu to Folia`() {
            formatToFormat(DocumentFormat.Conllu, DocumentFormat.Folia)
        }

        @Test
        fun `Conllu to Tsv`() {
            formatToFormat(DocumentFormat.Conllu, DocumentFormat.Tsv)
        }

        @Test
        fun `Conllu to TXT`() {
            formatToFormat(DocumentFormat.Conllu, DocumentFormat.Txt)
        }
    }
}