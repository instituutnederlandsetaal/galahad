package org.ivdnt.galahad.web.service

import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.JsonUtil
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class TaggersService : Logging {

    fun read(tagger: String): Tagger? = Tagger.readOrThrow(tagger)

    // TODO: could do with a refactor sometime
    fun taggerHealth(tagger: String): Boolean {
        val client = HttpClient.newBuilder().build()
        val tagger = Tagger.readOrThrow(tagger)
        val request = HttpRequest.newBuilder().uri(URI.create("${tagger.url}/health")).build()

        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val json = JsonUtil.mapper.readTree(response.body())
            json.get("healthy").asBoolean()
        } catch (e: Exception) {
            logger.error(
                "Failed to connect to tagger ${tagger.name} on url ${request.uri()}. Error: $e"
            )
            // If we cannot connect, there is no use in tagging, so just return
            false
        }
    }

    /**
     * Get the number of documents actively being tagged by retrieving the taggers' status dicts and
     * counting the number of pending and busy docs.
     */
    fun numActiveDocuments(): Int {
        var count = 0
        for (tagger in Tagger.taggers.values) {
            val name = tagger.name

            val restTemplate = RestTemplate()
            val endpoint = URL("${tagger.url}/status")
            val builder = UriComponentsBuilder.fromUri(endpoint.toURI())
            try {
                val res =
                    restTemplate.exchange(
                        builder.build().encode().toUri(),
                        HttpMethod.GET,
                        null,
                        String::class.java,
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
