package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus

class TaggerNotFoundException(
    identifier: String
) : Exception("Tagger $identifier not found"), RESTException {
    override val statusCode: HttpStatus = HttpStatus.NOT_FOUND
}