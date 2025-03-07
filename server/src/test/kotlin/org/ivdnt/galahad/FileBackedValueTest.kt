package org.ivdnt.galahad

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.File

internal class FileBackedValueTest {

    @Test
    fun getFile() {
        val file = File.createTempFile("temp", null)
        val fbv = org.ivdnt.galahad.filesystem.DiskValue<String>(file)
        fbv.write<String>("some characters")
        assertEquals("some characters", fbv.readOrThrow<String>())
    }
}