package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class CorpusNotFoundException(corpusID: UUID) : Exception("Corpus with ID $corpusID not found.")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class CorpusNameInvalidException(corpusName: String) :
    Exception("Corpus name $corpusName is invalid. No newlines and length 3-100.")

// UNAUTHORIZED is for login, FORBIDDEN is for access rights after login
@ResponseStatus(value = HttpStatus.FORBIDDEN)
class CorpusUnauthorizedException(action: String) : Exception("Unauthorized. $action")