package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidClassificationTypeException(message: String) : Exception(message)

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidMetricsTypeException(message: String) : Exception(message)