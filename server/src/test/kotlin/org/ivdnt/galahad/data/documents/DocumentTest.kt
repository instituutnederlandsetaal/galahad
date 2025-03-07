package org.ivdnt.galahad.data.documents

import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.formats.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.util.UUID
import kotlin.io.path.createTempDirectory

class DocumentTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `UUID stored in cache file`() {
        val doc = Resource.getDoc("all-formats/input/input.txt")
        assertDoesNotThrow { UUID.fromString(doc.metadata.uuid.toString()) }
    }

    @Nested
    inner class GenerateAsTest {
        // Yes, these tests could be in a for loop on DocumentFormat.entries,
        // but that would make the test output less readable.

        @Test
        fun `Convert Conllu to each other format`() {
            convertFormatToAllOthers(DocumentFormat.Conllu)
        }

        @Test
        fun `Convert TEI to each other format`() {
            convertFormatToAllOthers(DocumentFormat.TeiP5)
        }

        @Test
        fun `Convert Txt to each other format`() {
            convertFormatToAllOthers(DocumentFormat.Txt)
        }

        @Test
        fun `Convert TSV to each other format`() {
            convertFormatToAllOthers(DocumentFormat.Tsv)
        }

        @Test
        fun `Convert Folia to each other format`() {
            convertFormatToAllOthers(DocumentFormat.Folia)
        }

        @Test
        fun `Convert Naf to each other format`() {
            convertFormatToAllOthers(DocumentFormat.Naf)
        }

        private fun convertFormatToAllOthers(formatFrom: DocumentFormat) {
            val tempDir: File = createTempDirectory().toFile()
            // Skip the formats that are not supported
            if (formatFrom == DocumentFormat.TeiP4Legacy || formatFrom == DocumentFormat.TeiP5Legacy || formatFrom == DocumentFormat.Unknown) {
                return
            }

            // Create the file of formatFrom
            val inputFile = Resource.get("all-formats/input/input.${formatFrom.extension}")
            val tempFile = tempDir.resolve("input." + formatFrom.extension)
            inputFile.copyTo(tempFile, true)
            val doc = corpus.documents.createOrThrow(tempFile)
            val job = corpus.jobs.createOrThrow(TestConfig.TAGGER_NAME)
            // create the layer based on the plaintext parsing
            val plaintext = doc.plaintext
            val layer = LayerBuilder().loadLayerFromTSV("all-formats/input/pie-tdn.tsv", plaintext).build()
            job.setLayerForKey(doc.name,layer)

            // Convert to each other format
            for (formatTo in DocumentFormat.entries) {
                val meta = DocumentTransformMetadata(
                    corpus, job, doc, User("testUser"), formatTo
                )
                when (formatTo) {
                    // Skip the unsupported
                    DocumentFormat.TeiP4Legacy,
                    DocumentFormat.TeiP5Legacy,
                    DocumentFormat.Unknown,
                        -> assertThrows(Exception::class.java) { doc.convert(meta) }
                    // Convert to the supported
                    else -> {
                        // Skip the same format
                        if (formatFrom == formatTo) continue
                        println("Converting ${formatFrom.name} to ${formatTo.name}")
                        val result: File = doc.convert(meta)
                        val expected: File =
                            Resource.get("all-formats/output/from-$formatFrom-to-$formatTo.${formatTo.extension}")
                        val test = TestResult(expected.readText(), result.readText())
                        test.ignoreDate().ignoreUUID().ignoreTrailingWhiteSpaces().result()
                    }
                }
            }
        }
    }
}