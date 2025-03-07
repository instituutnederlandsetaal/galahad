package org.ivdnt.galahad.corpora.jobs

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.SOURCE_LAYER_NAME
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.documents.Document
import org.ivdnt.galahad.corpora.jobs.jobDocuments.JobDocument.DocumentProcessingStatus
import org.ivdnt.galahad.corpora.jobs.jobDocuments.JobDocuments
import org.ivdnt.galahad.evaluation.metrics.CorpusMetrics
import org.ivdnt.galahad.evaluation.metrics.FlatMetricType
import org.ivdnt.galahad.evaluation.metrics.METRIC_TYPES
import org.ivdnt.galahad.evaluation.metrics.Metrics
import org.ivdnt.galahad.exceptions.SourceLayerNotATaggerException
import org.ivdnt.galahad.exceptions.TaggerNoConnectionException
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import org.ivdnt.galahad.taggers.Tagger
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.io.File
import java.net.URI
import java.util.*

private const val DOCUMENT_JOBS_FOLDER = "documents"

/** Number of documents at the tagger per job */
private const val DOC_PARALLELIZATION_SIZE = 3
private const val IS_ACTIVE_FILE = "active"
private const val METADATA_FILE = "metadata.json"

/**
 * A job is saved to disk as a folder under jobs/ (managed by [Jobs]), with the following files:
 *
 * - documents/: a folder containing all documents in the job. A single document is represented by [org.ivdnt.galahad.corpora.jobs.jobDocuments.JobDocument]. These can be retrieved with [readOrThrow].
 * - _isActive: a file that stores whether the job is currently being processed by the tagger.
 * - assay.cache: a cache file storing the global [Metrics] of the job.
 * - state.cache: a cache file storing the [JobMetadata] of the job.
 */
class Job(
    dir: File, // the name of this directory is the name of the job/tagger
    val corpus: Corpus,
) : GalahadFolder(dir), Logging {

    val jobDocuments = JobDocuments(dir.resolve(DOCUMENT_JOBS_FOLDER))

    // Files
    val isActiveFile = dir.resolve(IS_ACTIVE_FILE)
    val metadataFile = dir.resolve(METADATA_FILE)

    // Values
    val hasResult: Boolean
        get() = name != SOURCE_LAYER_NAME && jobDocuments.readAll()
            .any { it.status == DocumentProcessingStatus.FINISHED }

    /**
     * Progress of the job based on the status of the [org.ivdnt.galahad.corpora.jobs.jobDocuments.JobDocument]s of this job.
     */
    val progress: Progress
        get() {
            // NOTE: The number of documents is not the same as the number of document jobs.
            // Example: after running a job, a user has added more documents to the corpus.
            // So for calculating progress, we need to look at the number of corpus documents.
            val docs = corpus.documents.readAll()
            // If a document is not in the list of documentJobs, it is pending by default.
            val statuses = docs.map { jobDocuments.readOrNull(it.name)?.status ?: DocumentProcessingStatus.PENDING }
            // For errors however, we can just look at the documentJobs.
            val errors = jobDocuments.readAll().mapNotNull { it.error?.let { error -> name to error } }.toMap()
            return Progress(
                pending = statuses.count { it == DocumentProcessingStatus.PENDING },
                processing = statuses.count { it == DocumentProcessingStatus.PROCESSING },
                failed = statuses.count { it == DocumentProcessingStatus.ERROR },
                finished = statuses.count { it == DocumentProcessingStatus.FINISHED },
                errors = errors
            )
        }

    /**
     * Whether the job is currently being processed (i.e. has sent files to the tagger to become tagged at some point).
     */
    var isActive: Boolean
        get() = isActiveFile.exists()
        set(value) {
            if (value) {
                isActiveFile.createNewFile()
            } else {
                isActiveFile.delete()
            }
        }

    val metadata: JobMetadata
        get() {
            deleteInactiveProcesses()
            return metadataCache.readOrCreate()
        }

    /**
     * The state of the job, which is cached in a file.
     * This is a very expensive operation, so we want to cache it.
     */
    private val metadataCache = object : ValidatedDiskValue<JobMetadata>(metadataFile) {
        // NOTE: we also check against the last modified of the documents folder: adding new docs should invalidate the cache.
        override fun isValid(lastModified: Long) =
            lastModified >= this@Job.lastModified && lastModified >= corpus.documents.lastModified

        override fun set() = JobMetadata.create(this@Job)
    }

    fun layer(doc: Document): Layer = layer(doc.name)
    fun layer(key: String): Layer = jobDocuments.readOrNull(key)?.layer ?: Layer.EMPTY
    fun setLayerForKey(key: String, layer: Layer) {
        jobDocuments.createOrThrow(key).layer = layer
    }

    //////////////////////////////////////////////////////
    // TODO: check everything below
    /////////////////////////////////////////////////////

    /**
     * The sum of the global [Metrics] score of all the documents of the job (as opposed to per PoS).
     * Cached in a file, as it is expensive.
     */
    val assay = object : ValidatedDiskValue<Map<String, FlatMetricType>>(
        file = dir.resolve("assay.cache")
    ) {
        override fun isValid(lastModified: Long): Boolean = lastModified >= this@Job.lastModified

        override fun set(): Map<String, FlatMetricType> {
            return CorpusMetrics(
                corpus = corpus, settings = METRIC_TYPES, hypothesis = name
            ).metricTypes.mapValues { it.value.toFlat() }
        }
    }

    private fun deleteInactiveProcesses() {
        val djs = jobDocuments.readAll()
        djs.filter { it.isProcessing }.forEach { documentJob ->
            // For each document that claims to be processing, verify if its pid is present at the tagger
            // If not, delete pid.
            try {
                val jsonStr: String? =
                    taggerRequest(this, "status/${documentJob.processingID!!}", HttpMethod.GET, String::class.java)
                val parser: Parser = Parser.default()
                val json: JsonObject = parser.parse(StringBuilder(jsonStr!!)) as JsonObject
                if (json.boolean("busy") == false && json.boolean("pending") == false) {
                    // The doc is either finished, has an error, or does not exist.
                    documentJob.cancel()
                    metadataFile.delete()
                }
            } catch (e: Exception) {
                // The tagger can't be reached, so no way to tell if the document is still processing.
                // If the tagger restarts, it does reprocess documents. Maybe including this one, so we keep it.
            }
        }
        if (djs.count { it.isProcessing } == 0 && isActive) {
            // Writing invalidates cache, so only write if isActive would change.
            isActive = false
        }
    }

    fun documentNameForProcessingIDOrNull(id: UUID): String? =
        jobDocuments.readAll().filter { it.processingID == id }.map { it.name }.firstOrNull()

    fun start() {
        if (name == SOURCE_LAYER_NAME) throw SourceLayerNotATaggerException()
        isActive = true
        next()
    }

    fun next() {
        if (name == SOURCE_LAYER_NAME) throw SourceLayerNotATaggerException()
        if (!isActive) return
        // Launch a coroutine so we can quickly return
        runBlocking {
            launch {
                uploadDocs()
            }
        }
    }

    /**
     * Upload documents to the tagger where they will be automatically processed.
     * Only ever upload as many files such that there are [DOC_PARALLELIZATION_SIZE] number of documents at the tagger.
     * Upon upload, a processingID is returned by the tagger, which we store in the respective [org.ivdnt.galahad.corpora.jobs.jobDocuments.JobDocument].
     */
    private fun uploadDocs() {
        // Quickly count the documents currently being processed
        val numCurrentlyBeingProcessed =
            jobDocuments.readAll().count { it.status == DocumentProcessingStatus.PROCESSING }

        // Upload the first documents to the tagger
        // Because the tag function might be activated multiple times,
        // We correct the number to remain with the defined parallelization
        val numberToUpload = 0.coerceAtLeast(DOC_PARALLELIZATION_SIZE - numCurrentlyBeingProcessed)

        // Upload the documents to the tagger
        corpus.documents.readAll().filter {
            val docJob = jobDocuments.readOrNull(it.name) ?: jobDocuments.createOrThrow(it.name)
            docJob.status == DocumentProcessingStatus.PENDING || docJob.status == DocumentProcessingStatus.ERROR
        }.take(numberToUpload).forEach {
            val processingID = postInputToTagger(it.plainTextFile)
            // Store the processingID, so we can match it with the incoming file later
            jobDocuments.readOrThrow(it.name).processingID = processingID
        }
    }

    /** Cancel the job by deleting all the currently processing input files at the tagger. */
    fun cancel() {
        if (name == SOURCE_LAYER_NAME) throw SourceLayerNotATaggerException()
        isActive = false
        jobDocuments.readAll().forEach { documentJob ->
            try {
                if (documentJob.isProcessing) {
                    deleteInputAtTagger(documentJob.processingID!!)
                }
            } catch (e: Exception) {
                // Ignore, so we cancel other documents even if one fails.
            } finally {
                documentJob.cancel()
            }
        }
    }

    fun delete() {
        cancel()
        dir.deleteRecursively()
    }

    /** Upload a single file to the tagger */
    private fun postInputToTagger(file: File): UUID {
        // Custom request entity due to file.
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val params = LinkedMultiValueMap<String, Any>()
        params.add("file", FileSystemResource(file))
        val requestEntity: HttpEntity<LinkedMultiValueMap<String, Any>> = HttpEntity(params, headers)

        val route = "input"
        val result: String? = taggerRequest(this, route, HttpMethod.POST, String::class.java, requestEntity)
        return UUID.fromString(result) ?: throw Exception("No result received when uploading file")
    }

    // Delete input files so that they won't be processed anymore.
    // For example because the user cancelled the job.
    private fun deleteInputAtTagger(pid: UUID) {
        val route = "input/$pid"
        taggerRequest(this, route, HttpMethod.DELETE, Void::class.java)
    }

    companion object {
        private fun <T : Any> taggerRequest(
            job: Job, route: String, method: HttpMethod, type: Class<T>,
            requestEntity: HttpEntity<LinkedMultiValueMap<String, Any>>? = null,
        ): T? {
            // Setup request.
            val restTemplate = RestTemplate()
            val endpoint = URI("${Tagger.readOrThrow(job.name, job.corpus).url}/$route")
            val builder = UriComponentsBuilder.fromUri(endpoint)
            // Send request.
            val responseEntity = try {
                restTemplate.exchange(
                    builder.build().encode().toUri(), method, requestEntity, // Allowed to be null
                    type
                )
            } catch (_: ResourceAccessException) {
                throw TaggerNoConnectionException(job.name)
            }
            // Handle result.
            if (responseEntity.statusCode != HttpStatus.OK) {
                throw Exception("$method file returned ${responseEntity.statusCode} with response ${responseEntity.body}")
            } else {
                return responseEntity.body
            }
        }
    }
}