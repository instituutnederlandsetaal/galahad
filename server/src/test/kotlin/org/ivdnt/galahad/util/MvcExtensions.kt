package org.ivdnt.galahad.util

import java.io.File
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.*

inline fun <reified T> MvcResult.andDeserialize(): T = JsonUtil.fromStr<T>(response.contentAsString)

fun MockMvc.patchJson(
    uri: String,
    body: Any,
    dsl: MockHttpServletRequestDsl.() -> Unit = {},
): ResultActionsDsl =
    patch(uri) {
        contentType = MediaType.APPLICATION_JSON
        content = JsonUtil.mapper.writeValueAsString(body)
        apply(dsl)
    }

fun MockMvc.postJson(
    uri: String,
    body: Any,
    dsl: MockHttpServletRequestDsl.() -> Unit = {},
): ResultActionsDsl =
    post(uri) {
        contentType = MediaType.APPLICATION_JSON
        content = JsonUtil.mapper.writeValueAsString(body)
        apply(dsl)
    }

fun MockMvc.uploadFile(
    uri: String,
    file: File,
    mediaType: String = MediaType.TEXT_PLAIN_VALUE,
    dsl: MockHttpServletRequestDsl.() -> Unit = {},
): ResultActionsDsl =
    multipart(uri) {
        file(MockMultipartFile("file", file.name, mediaType, file.readBytes()))
        apply(dsl)
    }
