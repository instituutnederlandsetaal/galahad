package org.ivdnt.galahad.util

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.core.type.TypeReference
import tools.jackson.core.util.DefaultIndenter
import tools.jackson.core.util.DefaultPrettyPrinter
import tools.jackson.core.util.Separators
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

object JsonUtil {
    val mapper: JsonMapper =
        JsonMapper.builder()
            .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
            .changeDefaultPropertyInclusion {
                it.withContentInclusion(JsonInclude.Include.NON_NULL)
            }
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .addModule(kotlinModule())
            .build()

    val prettyMapper: JsonMapper =
        JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
            .changeDefaultPropertyInclusion {
                it.withContentInclusion(JsonInclude.Include.NON_NULL)
            }
            .defaultPrettyPrinter(
                DefaultPrettyPrinter(
                        Separators.createDefaultInstance()
                            .withObjectNameValueSpacing(Separators.Spacing.AFTER)
                    )
                    .apply { indentArraysWith(DefaultIndenter()) }
            )
            .build()

    inline fun <reified T> fromStr(json: String): T =
        mapper.readValue(json, object : TypeReference<T>() {})
}
