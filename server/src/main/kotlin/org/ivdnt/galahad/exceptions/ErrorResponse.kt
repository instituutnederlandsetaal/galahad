package org.ivdnt.galahad.exceptions

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus

data class ErrorResponse(
    val error: HttpStatus,
    val message: String,
)