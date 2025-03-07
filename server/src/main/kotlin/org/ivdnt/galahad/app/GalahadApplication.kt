package org.ivdnt.galahad.app

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File
import java.math.BigDecimal
import java.util.*
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
const val TOKEN_FREQUENCY_URL = "$EVALUATION_URL/frequency"
const val METRICS_URL = "$EVALUATION_URL/metrics"
const val METRICS_SAMPLES_URL = "$METRICS_URL/download"
const val CONFUSION_URL = "$EVALUATION_URL/confusion"
const val CONFUSION_SAMPLES_URL = "$CONFUSION_URL/download"
const val EVALUATION_CSV_URL = "$EVALUATION_URL/download"
const val DOCUMENT_EVALUATION_URL = "$JOB_DOCUMENT_URL/evaluation"

const val DOCUMENTS_URL = "$CORPUS_URL/documents"
const val DOCUMENT_URL = "$DOCUMENTS_URL/{document}"
const val DOCUMENT_RAW_FILE_URL = "$DOCUMENT_URL/raw" // returns the blob of the raw document

var application_profile: String = System.getenv("spring.profiles.active") ?: "prod"

@Configuration
@ConfigurationProperties(prefix = "")
@EnableScheduling
class Config {
    lateinit var workDir: String

    @Bean
    fun getWorkingDirectory(): File = File(workDir)

    companion object {
        fun galahadVersion(): String = galahadVersionYaml().getProperty("GITHUB_REF_NAME")

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

@Configuration
@ConfigurationProperties(prefix = "spring.servlet.multipart")
class MultipartConfig {

    lateinit var maxFileSize: String
    lateinit var maxRequestSize: String

    val maxFilesSizeAsBytes: Long
        get() = toBytes(maxFileSize)

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
