package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus

class TagsetNotFoundException(
    identifier: String
) : Exception("Tagset with identifier $identifier not found."), RESTException {
    override val statusCode: HttpStatus = HttpStatus.NOT_FOUND
}