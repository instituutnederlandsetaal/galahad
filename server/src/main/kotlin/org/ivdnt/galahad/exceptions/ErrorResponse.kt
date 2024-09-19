package org.ivdnt.galahad.exceptions

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

data class ErrorResponse(
    @JsonProperty val error: HttpStatus,
    @JsonProperty val message: String,
)

interface RESTException {
    val statusCode: HttpStatus
}