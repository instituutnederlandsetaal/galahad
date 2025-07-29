package org.ivdnt.galahad.util

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.event.annotation.BeforeTestExecution
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.io.File
import kotlin.io.path.createTempDirectory

@TestConfiguration
class TestConfig {
    val workDir: String = createTempDirectory().toString()

    @Bean
    @Primary
    fun getWorkingDirectory(): File = File(workDir)

    companion object {
        const val TAGGER_NAME = "pie-tdn-all"
    }
}