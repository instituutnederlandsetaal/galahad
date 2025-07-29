package org.ivdnt.galahad.files

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class DiskValueTest {
    @Test
    fun getFile() {
        val file = File.createTempFile("temp", null)
        val diskValue = DiskValue<String>(file)
        diskValue.write<String>("some characters")
        Assertions.assertEquals("some characters", diskValue.readOrThrow<String>())
    }
}