package org.ivdnt.galahad.exceptions

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus

data class ErrorResponse(
    @JsonProperty val error: HttpStatus,
    @JsonProperty val message: String,
)