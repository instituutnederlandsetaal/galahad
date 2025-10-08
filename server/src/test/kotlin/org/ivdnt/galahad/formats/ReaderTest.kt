package org.ivdnt.galahad.formats

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Assertions.assertEquals

abstract class ReaderTest {
    protected abstract val format: DocumentFormat

    private val mapper: ObjectMapper = JsonMapper.builder()
        .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .build()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    protected fun assertLayerAndText(folder: String) {
        val layer = InternalFile.create(TestUtil.get("$folder/input.${format.extension}")).layer
        assertText(layer, folder)
        assertLayer(layer, folder)
    }

    private fun assertLayer(layer: Layer, folder: String) {
        val jsonExpected = TestUtil.get("$folder/layer.json").readText()
        val json = mapper.writeValueAsString(layer)
        assertEquals(cleanUUIDs(jsonExpected), cleanUUIDs(json))
    }

    private fun cleanUUIDs(text: String): String {
        // Simple regex to match UUIDs
        val uuidRegex = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
        return uuidRegex.replace(text, "UUID")
    }

    private fun assertText(layer: Layer, folder: String) {
        val text = layer.toString().trim()
        val expected = TestUtil.get("$folder/plaintext.txt").readText().trim()
        assertEquals(expected, text)
    }
}