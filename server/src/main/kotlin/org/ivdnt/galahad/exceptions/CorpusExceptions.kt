package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class CorpusNotFoundException(uuid: String) : Exception("Corpus $uuid not found.")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class CorpusInvalidException(message: String) : Exception(message)
