package org.ivdnt.galahad.files

import java.io.File
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.util.JsonUtil
import org.ivdnt.galahad.util.LayerBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DiskValueTest {
    @Test
    fun getFile() {
        val file = File.createTempFile("temp", null)
        val diskValue = DiskValue<String>(file)
        diskValue.write<String>("some characters")
        assertEquals("some characters", diskValue.readOrThrow<String>())
    }

    @Test
    fun getLayer() {
        val file = File.createTempFile("temp", null)
        val diskValue = DiskValue<Layer>(file)
        val layer = LayerBuilder().loadText("some text").build()
        diskValue.write<Layer>(layer)
        val expected = JsonUtil.prettyMapper.writeValueAsString(layer)
        val actual = JsonUtil.prettyMapper.writeValueAsString(diskValue.readOrThrow<Layer>())
        assertEquals(expected, actual)
    }
}
