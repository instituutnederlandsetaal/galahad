package org.ivdnt.galahad.app

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.filter.CommonsRequestLoggingFilter
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

// This is a possibly incomplete list of all the endpoints
// For a complete overview better go to
// SWAGGER_API_URL
const val BASE_URL = "/"
const val SWAGGER_API_URL = "/swagger-ui/index.html"

const val TAGSETS_URL = "/tagsets"
const val VERSION_URL = "/version"

const val TAGGERS_URL = "/taggers"
const val TAGGER_URL = "$TAGGERS_URL/{tagger}"
const val TAGGER_HEALTH_URL = "$TAGGER_URL/health"

const val BENCHMARKS_URL = "/benchmarks"
const val BENCHMARK_URL = "$BENCHMARKS_URL/{corpus}/{job}"

const val INTERNAL_JOBS_URL = "/internal/jobs"
const val INTERNAL_JOBS_RESULT_URL = "$INTERNAL_JOBS_URL/result"
const val INTERNAL_JOBS_ERROR_URL = "$INTERNAL_JOBS_URL/error"

const val CORPORA_URL = "/corpora"
const val DATASETS_CORPORA_URL = "/datasets_corpora"
const val CORPUS_URL = "$CORPORA_URL/{corpus}"

const val JOBS_URL = "$CORPUS_URL/jobs"
const val JOB_URL = "$JOBS_URL/{job}"
const val JOB_DOCUMENT_URL = "$JOB_URL/documents/{document}"

const val EVALUATION_URL = "$JOB_URL/evaluation"
const val DISTRIBUTION_URL = "$EVALUATION_URL/distribution"
const val METRICS_URL = "$EVALUATION_URL/metrics"
const val METRICS_SAMPLES_URL = "$METRICS_URL/download"
const val CONFUSION_URL = "$EVALUATION_URL/confusion"
const val CONFUSION_SAMPLES_URL = "$CONFUSION_URL/download"
const val EVALUATION_CSV_URL = "$EVALUATION_URL/download"

const val DOCUMENTS_URL = "$CORPUS_URL/documents"
const val DOCUMENT_URL = "$DOCUMENTS_URL/{document}"
const val DOCUMENT_RAW_FILE_URL = "$DOCUMENT_URL/raw" // returns the blob of the raw document

var application_profile: String = System.getenv("spring.profiles.active") ?: "prod"
fun String.runCommand(workingDir: File, timeout: Long = 60): String? {
    try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .inheritIO()
            .start()

        proc.waitFor(timeout, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

@Configuration
@ConfigurationProperties(prefix = "")
class Config {

    lateinit var workDir: String

    @Bean
    fun getWorkingDirectory(): File {
        return File(workDir)
    }

    companion object {
        fun galahadVersion(): String {
            return galahadVersionYaml().getProperty("GITHUB_REF_NAME")
        }

        fun galahadVersionYaml(): Properties {
            val versionStream = this::class.java.classLoader.getResource("version.yml")!!.openStream()
            val versionProperties = Properties()
            versionProperties.load(versionStream)
            return versionProperties
        }
    }
}

@ComponentScan("org.ivdnt.galahad")
@SpringBootApplication
class GalahadApplication {
    @Bean
    fun customOpenAPI(): OpenAPI {
        var api = OpenAPI()
            .components(Components())
            .info(
                io.swagger.v3.oas.models.info.Info()
                    .title("GaLAHaD API")
                    .version(Config.galahadVersion())
                    .license(License().name("Apache 2.0").url("https://www.apache.org/licenses/"))
                    .description("Generating Linguistic Annotations for Historical Dutch")
                    .contact(Contact().name("GaLAHaD GitHub").url("https://github.com/inl/galahad"))
            )
        if (application_profile.contains("prod")) {
            api = api.servers(listOf(Server().url("/galahad/api").description("GaLAHaD API")))
        }
        return api
    }
}

fun main(args: Array<String>) {
    runApplication<GalahadApplication>(*args)
}

@RestController
class ApplicationController : ErrorController, Logging {
    @Autowired
    private val request: HttpServletRequest? = null
    @Autowired
    private val response: HttpServletResponse? = null

    @Operation(
        summary = "Get version information",
        description = "Get version information and GitHub build information and commit version.",
        responses = [
            ApiResponse(
                description = "Version information."
            )
        ]
    )
    @CrossOrigin
    @GetMapping(VERSION_URL)
    fun getVersion(): Map<String, String> {
        return Config.galahadVersionYaml().entries.associate { it.key.toString() to it.value.toString() }
    }

    @Hidden
    @CrossOrigin
    @GetMapping(BASE_URL)
    fun getApplication(): ResponseEntity<Void> {
        // Since we have nothing to show at this URL, we redirect to the API UI instead
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(request?.contextPath + SWAGGER_API_URL))
            .build()
    }

    @Operation(
        summary = "Get user information",
        description = "Get the username and whether the user is an admin.",
        responses = [
            ApiResponse(
                description = "User information."
            )
        ]
    )
    @CrossOrigin
    @GetMapping("/user")
    fun getUser(): User {
        return User.getUserFromRequestOrThrow(request)
    }


    @RequestMapping("/error")
	@Hidden
	@CrossOrigin
	fun handleError(request: HttpServletRequest): ErrorResponse {
        // Get the default status code (probably 500), or override it with the actual status code if it is a RESTException.
		var statusCode = HttpStatus.valueOf( request.getAttribute("jakarta.servlet.error.status_code") as Int? ?: 500 )
		val jakartaException = request.getAttribute("jakarta.servlet.error.exception") as Exception?
        response?.status = statusCode.value()

        // Error message
        var jakartaErrorMsg = request.getAttribute("jakarta.servlet.error.message") as String?
        if (jakartaErrorMsg?.isBlank() == true) {
            jakartaErrorMsg = null
        }
		val springException = request.getAttribute("org.springframework.web.servlet.DispatcherServlet.EXCEPTION") as Exception?
		val errorMsg = jakartaErrorMsg ?: jakartaException?.cause?.message ?: springException?.message ?: "No error message available."
		return ErrorResponse(statusCode, errorMsg)
	}
}

@Configuration
@ConfigurationProperties(prefix = "spring.servlet.multipart")
class MultipartConfig {

    lateinit var maxFileSize: String
    lateinit var maxRequestSize: String

    val maxFilesSizeAsBytes: Long
        get() {
            return toBytes(maxFileSize)
        }

    companion object {
        fun toBytes(filesize: String?): Long {
            var returnValue: Long = -1
            val patt: Pattern = Pattern.compile("([\\d.]+)([GMK]B)", Pattern.CASE_INSENSITIVE)
            val matcher: Matcher = patt.matcher(filesize)
            val powerMap: MutableMap<String, Int> = HashMap()
            powerMap["GB"] = 3
            powerMap["MB"] = 2
            powerMap["KB"] = 1
            if (matcher.find()) {
                val number: String = matcher.group(1)
                val pow = powerMap[matcher.group(2).uppercase()]!!
                var bytes = BigDecimal(number)
                bytes = bytes.multiply(BigDecimal.valueOf(1024).pow(pow))
                returnValue = bytes.longValueExact()
            }
            return returnValue
        }
    }
}

@Configuration
class RequestLoggingFilterConfig {
    @Bean
    fun logFilter(): CommonsRequestLoggingFilter {
        val filter = CommonsRequestLoggingFilter()
        filter.setIncludeQueryString(true)
        filter.setIncludePayload(true)
        filter.setMaxPayloadLength(10000)
        filter.setIncludeHeaders(true)
        filter.setAfterMessagePrefix("REQUEST DATA : ")
        return filter
    }
}