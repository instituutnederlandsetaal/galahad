package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus

data class ErrorResponse(
    val error: HttpStatus,
    val message: String,
)
