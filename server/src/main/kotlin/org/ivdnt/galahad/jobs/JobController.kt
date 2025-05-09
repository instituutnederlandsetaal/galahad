package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.exceptions.SourceLayerNotATaggerException
import org.ivdnt.galahad.exceptions.TaggerNoConnectionException
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.taggers.Tagger
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder
import java.io.File
import java.net.URI
import java.util.UUID
import kotlin.io.path.createTempFile

object JobController {
    private const val DOC_PARALLELIZATION_SIZE = 3

    val queue: ArrayDeque<Job> = ArrayDeque<Job>()
    val tasks: MutableMap<UUID, Task> = mutableMapOf()
    var active: Boolean = false

    class Task(val job: Job, val document: String) {
        fun finish(file: File) {
            job.results.createOrThrow(document).layer = InternalFile.create(file).layer
        }
    }

    fun queue(job: Job) {
        if (job.name == SOURCE_LAYER_NAME) throw SourceLayerNotATaggerException()
        queue += job
        start()
    }

    fun unqueue(job: Job) {
        if (job.name == SOURCE_LAYER_NAME) throw SourceLayerNotATaggerException()
        queue -= job
        // TODO: stop
    }

    fun start() {
        if (!active) {
            // first job if exists
            queue.firstOrNull()?.let { job ->
                // get all docs each time, because new ones might be added while tagging
                val docs = job.corpus.documents.readAllSequence()
                // untagged are those for which exists no result
                val untagged = docs.filter { job.results.readOrNull(it.name) == null }
                // create a task for the first one
                val task = Task(job, untagged.first().name)
                val id = tag(task)
                tasks[id] = task
            }
        }
    }

    fun tag(task: Task): UUID {
        active = true
        val url = "${Tagger.readOrThrow(task.job.name).url}/input"
        val text = task.job.corpus.jobs.readOrThrow(SOURCE_LAYER_NAME).results.readOrThrow(task.document).layer.toString()
        val file = createTempFile().toFile().also { it.writeText(text) }
        val entity = HttpEntity(
            LinkedMultiValueMap<String, Any>().apply {
                add("file", FileSystemResource(file))
            },
            HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        )
        val response = RestTemplate().postForEntity<String>(
            url, entity
        )
        if (response.statusCode != HttpStatus.OK) {
            throw Exception("Error while tagging: ${response.statusCode}")
        }
        return UUID.fromString(response.body) ?: throw Exception("No UUID received from tagger")
    }

    fun receive(uuid: UUID, file: File) {
        val task = tasks[uuid] ?: throw Exception("No task found for UUID $uuid")
        task.finish(file)
        active = false
        tasks.remove(uuid)
        // if no untagged documents are left, remove the job from the queue
        val numUntagged = task.job.corpus.documents.readAll()
            .count { task.job.results.readOrNull(it.name) == null }
        if (numUntagged == 0) {
            queue -= task.job
        }
        // next document, or next job if all documents are tagged
        start()
    }
}