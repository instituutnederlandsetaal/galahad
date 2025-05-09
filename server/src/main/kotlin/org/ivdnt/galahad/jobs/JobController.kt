package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.documents.Document
import org.ivdnt.galahad.exceptions.SourceLayerNotATaggerException
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.taggers.Tagger
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.io.File
import java.util.UUID
import kotlin.collections.ArrayDeque
import kotlin.collections.count
import kotlin.collections.minusAssign
import kotlin.collections.plusAssign
import kotlin.io.path.createTempFile

object JobController {
    private val queue: ArrayDeque<Job> = ArrayDeque<Job>()
    private var task: Task? = null
    val queueSize: Int get() = queue.size

    fun queue(job: Job) {
        if (job.name == SOURCE_LAYER_NAME) throw SourceLayerNotATaggerException()
        queue += job
        start()
    }

    fun unqueue(job: Job) {
        if (job.name == SOURCE_LAYER_NAME) throw SourceLayerNotATaggerException()
        queue -= job
        stop(job)
    }

    fun receive(uuid: UUID, file: File) {
        if (task == null || uuid != task?.uuid) {
            throw Exception("No task found for UUID $uuid")
        }
        task!!.finish(file)
        // if no untagged documents are left, remove the job from the queue
        val numUntagged = task!!.job.corpus.documents.readAll().count { task!!.job.results.readOrNull(it.name) == null }
        if (numUntagged == 0) {
            queue -= task!!.job
        }
        // next document, or next job if all documents are tagged
        task = null
        start()
    }

    private fun start() {
        // Only one task at a time.
        if (task == null) {
            // First job in queue if it exists.
            queue.firstOrNull()?.let { job ->
                // Get all docs each time, because new ones might be added while tagging.
                val docs = job.corpus.documents.readAllSequence()
                // Untagged are those for which no result exists.
                val untagged = docs.filter { job.results.readOrNull(it.name) == null }
                // Is there even an untagged document?
                val doc = untagged.firstOrNull()
                if (doc == null) {
                    // Somehow no untagged documents left, remove the job from the queue anyway.
                    queue -= job
                } else {
                    // Tag and register task.
                    val id = tag(job, doc)
                    task = Task(id, job, doc.name)
                }
            }
        }
    }

    private fun stop(job: Job) {
        if (task?.job != job) {
            return // No task to stop.
        }
        // Send stop signal to tagger.
        val url = "${Tagger.readOrThrow(job.name).url}/input/${task?.uuid}"
        RestTemplate().delete(url)
        task = null
    }

    private fun tag(job: Job, doc: Document): UUID {
        val url = "${Tagger.readOrThrow(job.name).url}/input"
        val text = job.corpus.jobs.readOrThrow(SOURCE_LAYER_NAME).results.readOrThrow(doc.name).layer.toString()
        val file = createTempFile().toFile().also { it.writeText(text) }
        val entity = HttpEntity(LinkedMultiValueMap<String, Any>().apply {
            add("file", FileSystemResource(file))
        }, HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA })
        val response = RestTemplate().postForEntity<String>(
            url, entity
        )
        if (response.statusCode != HttpStatus.OK) {
            throw Exception("Error while tagging: ${response.statusCode}")
        }
        return UUID.fromString(response.body) ?: throw Exception("No UUID received from tagger")
    }

    private class Task(val uuid: UUID, val job: Job, val doc: String) {
        fun finish(file: File) {
            job.results.createOrThrow(doc).layer = InternalFile.create(file).layer
        }
    }
}