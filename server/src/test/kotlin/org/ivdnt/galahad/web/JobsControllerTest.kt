package org.ivdnt.galahad.web

import java.util.*
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.jobs.JobMetadata
import org.ivdnt.galahad.jobs.Progress
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.web.controller.JobsController
import org.ivdnt.galahad.web.controller.TaggersController
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = ["server.port=8010", "spring.main.allow-bean-definition-overriding=true"],
)
@Disabled
class JobsControllerTest(
    @Autowired val rest: TestRestTemplate,
    @Autowired val config: Config,
    @Autowired val ctrl: JobsController,
    @Autowired val taggers: TaggersController,
) {

    @Test
    fun postJob() {
        val corpus = TestUtil.createCorpus(config)
        val doc = corpus.documents.createOrThrow(TestUtil.get("all-formats/input/input.tei.xml"))
        val uuid = corpus.uuid

        Assertions.assertEquals(
            taggers.getTaggers().count() + 1,
            getJobs(uuid).size,
        ) // +1 for the sourceLayer
        var progress: Progress =
            rest
                .postForEntity("/corpora/$uuid/jobs/pie-tdn", getHeaders(), Progress::class.java)
                .body!!
        Assertions.assertEquals(1, progress.total)
        Assertions.assertTrue(progress.busy)

        Thread.sleep(3000)

        // poll progress
        progress = pollProgress(uuid, TestUtil.TAGGER_NAME)
        Assertions.assertFalse(progress.busy)
        Assertions.assertEquals(1, progress.finished)

        // check result
        val resultPreview = getDocumentJobResult(uuid, TestUtil.TAGGER_NAME, doc.name)
        Assertions.assertTrue(resultPreview.summary.annotations[Annotation.TOKEN]!! > 0)
        Assertions.assertTrue(resultPreview.preview.terms.isNotEmpty())
    }

    private fun pollProgress(uuid: UUID, job: String): Progress {
        return rest
            .exchange(
                "/corpora/$uuid/jobs/$job/progress",
                HttpMethod.GET,
                getHeaders(),
                Progress::class.java,
            )
            .body!!
    }

    private fun getJobs(uuid: UUID): Set<JobMetadata> {
        return rest
            .exchange(
                "/corpora/$uuid/jobs?includePotentialJobs=true",
                HttpMethod.GET,
                getHeaders(),
                object : ParameterizedTypeReference<Set<JobMetadata>>() {},
            )
            .body!!
    }

    private fun getDocumentJobResult(uuid: UUID, job: String, document: String): Layer {
        return rest
            .exchange(
                "/corpora/$uuid/jobs/$job/documents/$document/result",
                HttpMethod.GET,
                getHeaders(),
                Layer::class.java,
            )
            .body!!
    }

    private fun getHeaders(): HttpEntity<Any> {
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add("remote_user", "testUser")
        return HttpEntity(null, headers)
    }
}
