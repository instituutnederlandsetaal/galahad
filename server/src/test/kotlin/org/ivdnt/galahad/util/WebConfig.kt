package com.myproject.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport

@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurationSupport() {
    public override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>?>) {
        converters.add(jackson2HttpMessageConverter())
    }

    @Bean
    fun jackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter {
        val converter = MappingJackson2HttpMessageConverter()
        val builder = this.jacksonBuilder()
        converter.setObjectMapper(builder.build())
        return converter
    }

    fun jacksonBuilder(): Jackson2ObjectMapperBuilder {
        val builder = Jackson2ObjectMapperBuilder()
        builder.serializationInclusion(JsonInclude.Include.NON_NULL)
        return builder
    }
}