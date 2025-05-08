package org.ivdnt.galahad.app

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.ivdnt.galahad.documents.DocumentFormat
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File
import java.math.BigDecimal
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

// This is a possibly incomplete list of all the endpoints
// For a complete overview better go to
// SWAGGER_API_URL
const val BASE_URL: String = "/"
const val SWAGGER_API_URL: String = "/swagger-ui/index.html"

const val TAGSETS_URL: String = "/tagsets"
const val VERSION_URL: String = "/version"

const val TAGGERS_URL: String = "/taggers"
const val TAGGER_URL: String = "$TAGGERS_URL/{tagger}"
const val TAGGER_HEALTH_URL: String = "$TAGGER_URL/health"

const val BENCHMARKS_URL: String = "/benchmarks"
const val BENCHMARK_URL: String = "$BENCHMARKS_URL/{corpus}/{job}"

const val INTERNAL_JOBS_URL: String = "/internal/jobs"
const val INTERNAL_JOBS_RESULT_URL: String = "$INTERNAL_JOBS_URL/result"
const val INTERNAL_JOBS_ERROR_URL: String = "$INTERNAL_JOBS_URL/error"

const val CORPORA_URL: String = "/corpora"
const val DATASETS_CORPORA_URL: String = "/datasets_corpora"
const val CORPUS_URL: String = "$CORPORA_URL/{corpus}"

const val JOBS_URL: String = "$CORPUS_URL/jobs"
const val JOB_URL: String = "$JOBS_URL/{job}"
const val JOB_DOCUMENT_URL: String = "$JOB_URL/documents/{document}"

const val EVALUATION_URL: String = "$JOB_URL/evaluation"
const val DISTRIBUTION_URL: String = "$EVALUATION_URL/distribution"
const val TOKEN_FREQUENCY_URL: String = "$EVALUATION_URL/frequency"
const val ENTITIES_URL: String = "$JOB_DOCUMENT_URL/entities"
const val METRICS_URL: String = "$EVALUATION_URL/metrics"
const val METRICS_SAMPLES_URL: String = "$METRICS_URL/download"
const val CONFUSION_URL: String = "$EVALUATION_URL/confusion"
const val CONFUSION_SAMPLES_URL: String = "$CONFUSION_URL/download"
const val EVALUATION_CSV_URL: String = "$EVALUATION_URL/download"
const val DOCUMENT_EVALUATION_URL: String = "$JOB_DOCUMENT_URL/evaluation"

const val DOCUMENTS_URL: String = "$CORPUS_URL/documents"
const val DOCUMENT_URL: String = "$DOCUMENTS_URL/{document}"
const val DOCUMENT_RAW_FILE_URL: String = "$DOCUMENT_URL/raw" // returns the blob of the raw document

var application_profile: String = System.getenv("spring.profiles.active") ?: "prod"

@Configuration
@ConfigurationProperties(prefix = "")
@EnableScheduling
class Config {
    lateinit var workDir: String

    @Bean
    fun getWorkingDirectory(): File = File(workDir)

    companion object {
        val galahadVersionYaml: Properties by lazy { Properties().apply { load(Config::class.java.getResourceAsStream("/version.yml")) } }
        val galahadVersion: String by lazy { galahadVersionYaml.getProperty("GITHUB_REF_NAME") }
    }
}

@ComponentScan("org.ivdnt.galahad")
@SpringBootApplication
class Galahad {
    @Bean
    fun customOpenAPI(): OpenAPI {
        var api = OpenAPI().components(Components()).info(
            Info().title("GaLAHaD API").version(Config.galahadVersion)
                .license(License().name("Apache 2.0").url("https://www.apache.org/licenses/"))
                .description("Generating Linguistic Annotations for Historical Dutch")
                .contact(Contact().name("GaLAHaD GitHub").url("https://github.com/instituutnederlandsetaal/galahad"))
        )
        if ("prod" in application_profile) {
            api = api.servers(listOf(Server().url("/galahad/api").description("GaLAHaD API")))
        }
        return api
    }
}

fun main(args: Array<String>) {
    runApplication<Galahad>(*args)
}

@Configuration
@ConfigurationProperties(prefix = "spring.servlet.multipart")
class MultipartConfig {

    lateinit var maxFileSize: String
    lateinit var maxRequestSize: String

    val maxFilesSizeAsBytes: Long get() = toBytes(maxFileSize)

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
class DocumentFormatConverter : Converter<String, DocumentFormat> {
    override fun convert(source: String): DocumentFormat = DocumentFormat.fromString(source)
}