package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.createCorpus
import org.ivdnt.galahad.corpora.jobs.JobMetadata
import org.ivdnt.galahad.corpora.jobs.Progress
import org.ivdnt.galahad.annotations.LayerMetadata
import org.ivdnt.galahad.formats.Resource
import org.ivdnt.galahad.web.controller.JobsController
import org.ivdnt.galahad.web.controller.TaggersController
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = ["server.port=8010", "spring.main.allow-bean-definition-overriding=true"]
)
class JobsControllerTest(
    @Autowired val rest: TestRestTemplate,
    @Autowired val config: Config,
    @Autowired val ctrl: JobsController,
    @Autowired val taggers: TaggersController,
) {

    @Disabled
    @Test
    fun postJob() {
        val corpus = createCorpus(config)
        val doc = corpus.documents.createOrThrow(Resource.get("all-formats/input/input.tei.xml"))
        val uuid = corpus.immutableMetadata.uuid

        assertEquals(taggers.getTaggers().size + 1, getJobs(uuid).size) // +1 for the sourceLayer
        var progress: Progress =
            rest.postForEntity("/corpora/$uuid/jobs/pie-tdn", getHeaders(), Progress::class.java).body!!
        assertEquals(1, progress.total)
        assertTrue(progress.busy)

        Thread.sleep(3000)

        // poll progress
        progress = pollProgress(uuid, TestConfig.TAGGER_NAME)
        assertFalse(progress.busy)
        assertEquals(1, progress.finished)

        // check result
        val resultPreview = getDocumentJobResult(uuid, TestConfig.TAGGER_NAME, doc.name)
        assertEquals(TestConfig.TAGGER_NAME, resultPreview.name)
        assertTrue(resultPreview.summary.tokens > 0)
        assertTrue(resultPreview.preview.terms.isNotEmpty())
        assertTrue(resultPreview.preview.wordforms.isNotEmpty())
    }

    private fun pollProgress(uuid: UUID, job: String): Progress {
        return rest.exchange(
            "/corpora/$uuid/jobs/$job/progress", HttpMethod.GET, getHeaders(), Progress::class.java
        ).body!!
    }

    private fun getJobs(uuid: UUID): Set<JobMetadata> {
        return rest.exchange("/corpora/$uuid/jobs?includePotentialJobs=true",
            HttpMethod.GET,
            getHeaders(),
            object : ParameterizedTypeReference<Set<JobMetadata>>() {}).body!!
    }

    private fun getDocumentJobResult(uuid: UUID, job: String, document: String): LayerMetadata {
        return rest.exchange(
            "/corpora/$uuid/jobs/$job/documents/$document/result",
            HttpMethod.GET,
            getHeaders(),
            LayerMetadata::class.java
        ).body!!
    }

    private fun getHeaders(): HttpEntity<Any> {
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add("remote_user", "testUser")
        return HttpEntity(null, headers)
    }
}