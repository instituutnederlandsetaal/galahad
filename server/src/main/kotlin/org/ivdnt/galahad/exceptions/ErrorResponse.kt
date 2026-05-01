package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus

class ErrorResponse(val error: HttpStatus, val message: String)
