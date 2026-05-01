package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class CorpusNotFoundException(uuid: String) : Exception("Corpus $uuid not found.")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class CorpusInvalidException(message: String) : Exception(message)

// HttpStatus UNAUTHORIZED is for login, FORBIDDEN is for access rights after login
@ResponseStatus(value = HttpStatus.FORBIDDEN)
class CorpusUnauthorizedException(action: String) : Exception("Unauthorized. $action")
