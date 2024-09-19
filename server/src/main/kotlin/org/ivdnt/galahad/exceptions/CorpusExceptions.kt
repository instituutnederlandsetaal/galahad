package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus
import java.util.*

class CorpusNotFoundException(
    corpusID: UUID
) : Exception("Corpus with ID $corpusID not found."), RESTException {
    override val statusCode: HttpStatus = HttpStatus.NOT_FOUND
}

class CorpusNameInvalidException(
    corpusName: String
) : Exception("Corpus name $corpusName is invalid. No newlines and length 3-100."), RESTException {
    override val statusCode: HttpStatus = HttpStatus.BAD_REQUEST
}

class CorpusUnauthorizedException(action: String) : Exception("Unauthorized. $action"), RESTException {
    // UNAUTHORIZED is for login, FORBIDDEN is for access rights after login
    override val statusCode: HttpStatus = HttpStatus.FORBIDDEN
}