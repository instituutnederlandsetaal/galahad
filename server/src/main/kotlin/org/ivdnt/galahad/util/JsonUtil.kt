package org.ivdnt.galahad.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonUtil {
    val mapper: ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
}