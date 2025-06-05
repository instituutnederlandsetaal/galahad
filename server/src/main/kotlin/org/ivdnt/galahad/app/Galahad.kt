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