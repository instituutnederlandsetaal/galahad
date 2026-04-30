package org.ivdnt.galahad.util

import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.event.annotation.BeforeTestExecution
import java.io.File
import kotlin.io.path.createTempDirectory

@TestConfiguration
class TestConfig {
    @Bean
    @Primary
    fun getWorkingDirectory(): File = File(workDir)

    companion object {
        var workDir: String = createTempDirectory().toString()
        fun reset() {
            File(workDir).listFiles()?.forEach { it.deleteRecursively() }
        }
    }
}