package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus

class DocumentNotFoundException(
    documentName: String
) : Exception("Document $documentName not found."), RESTException {
    override val statusCode: HttpStatus = HttpStatus.NOT_FOUND
}

class DocumentInvalidFormatException(
    documentName: String,
    details: String? = null
) : Exception("Document $documentName has an invalid format. ${details ?: ""}"), RESTException {
    override val statusCode: HttpStatus = HttpStatus.BAD_REQUEST
}