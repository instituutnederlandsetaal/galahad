package org.ivdnt.galahad.files

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class FileBackedValueTest {
    @Test
    fun getFile() {
        val file = File.createTempFile("temp", null)
        val fbv = DiskValue<String>(file)
        fbv.write<String>("some characters")
        Assertions.assertEquals("some characters", fbv.readOrThrow<String>())
    }
}