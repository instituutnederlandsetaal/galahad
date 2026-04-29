package org.ivdnt.galahad.util

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.*
import java.io.File

fun MockMvc.patchJson(uri: String, body: Any, dsl: MockHttpServletRequestDsl.() -> Unit = {}): ResultActionsDsl =
    patch(uri) {
        contentType = MediaType.APPLICATION_JSON
        content = JsonUtil.mapper.writeValueAsString(body)
        apply(dsl)
    }

fun MockMvc.postJson(uri: String, body: Any, dsl: MockHttpServletRequestDsl.() -> Unit = {}): ResultActionsDsl =
    post(uri) {
        contentType = MediaType.APPLICATION_JSON
        content = JsonUtil.mapper.writeValueAsString(body)
        apply(dsl)
    }

inline fun <reified T> MvcResult.andDeserialize(): T = JsonUtil.fromStr<T>(response.contentAsString)

fun MockMvc.uploadFile(file: File, corpus: Corpus, mediaType: String = MediaType.TEXT_PLAIN_VALUE): MvcResult {
    // Create file
    val mockFile = MockMultipartFile(
        "file", file.name, mediaType, file.readBytes()
    )
    // Perform request
    val uuid = corpus.uuid
    return multipart("/corpora/$uuid/documents") {
        file(mockFile)
        headers(::assignHeaders)
    }.andReturn()
}