package org.ivdnt.galahad.web.service

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.taggers.TaggerHealth
import org.ivdnt.galahad.taggers.TaggerHealthStatus
import org.ivdnt.galahad.util.JsonUtil
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class TaggersService : Logging {

    fun readAll(): Iterable<Tagger> = Tagger.taggers.values
    fun read(tagger: String): Tagger? = Tagger.readOrThrow(tagger)
    fun taggerHealth(tagger: String): TaggerHealth {
        // If there are multiple replicas for the same service, we only get health check response from one replica.
        // However, we still think it is representative/informative
        val client = HttpClient.newBuilder().build()
        val tagger = Tagger.readOrThrow(tagger)
        val request = HttpRequest.newBuilder().uri(URI.create("${tagger.url}/health")).build()

        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val json = JsonUtil.mapper.readTree(response.body())
            val healthy = json.get("healthy").asBoolean()
            val queueSizeAtTagger = json.get("queueSizeAtTagger").asInt()
            val processingSpeed = json.get("processingSpeed").asInt()

            // Note that this queuesize only represents the queue present at a single instance of the tagger,
            // also the processing speed is of a single tagger
            // not any pending documents on the server
            // We could get this by count all pending document for this tagger in all corpora
            TaggerHealth(
                status = if (healthy) TaggerHealthStatus.HEALTHY else TaggerHealthStatus.NOT_HEALTHY,
                queueSizeAtTagger = queueSizeAtTagger,
                processingSpeed = processingSpeed,
                message = "Can connect to tagger. Taggers health response: ${response.body()}"
            )
        } catch (e: Exception) {
            logger.error("Failed to connect to tagger ${tagger.id} on url ${request.uri()}. Error: $e")
            // If we cannot connect, there is no use in tagging, so just return
            return TaggerHealth(status = TaggerHealthStatus.ERROR, message = "Cannot connect to tagger")
        }
    }

    /**
     * Get the number of documents actively being tagged by retrieving the taggers' status dicts
     * and counting the number of pending and busy docs.
     */
    fun numActiveDocuments(): Int {
        var count = 0
        for (tagger in Tagger.taggers.values) {
            val name = tagger.id

            val restTemplate = RestTemplate()
            val endpoint = URL("${tagger.url}/status")
            val builder = UriComponentsBuilder.fromUri(endpoint.toURI())
            try {
                val res = restTemplate.exchange(
                    builder.build().encode().toUri(), HttpMethod.GET, null, String::class.java
                )
                val jsonStr: String? = res.body
                val json = JsonUtil.mapper.readTree(jsonStr)
                json.forEach {
                    val pending = it.get("pending").asBoolean()
                    val busy = it.get("busy").asBoolean()
                    if (pending || busy) {
                        count++
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to connect to tagger $name. Error: $e")
            }
        }
        return count
    }
}