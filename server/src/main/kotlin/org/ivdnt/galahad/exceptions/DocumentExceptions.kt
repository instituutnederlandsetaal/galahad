package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus

class DocumentNotFoundException(
    documentName: String
) : Exception("Document $documentName not found."), RESTException {
    override val statusCode: HttpStatus = HttpStatus.NOT_FOUND
}

class DocumentInvalidException(
    documentName: String,
    details: String? = null
) : Exception("Document $documentName is invalid. ${details ?: ""}"), RESTException {
    override val statusCode: HttpStatus = HttpStatus.BAD_REQUEST
}

class InvalidDocumentFormatException(
    details: String
) : Exception(details), RESTException {
    override val statusCode: HttpStatus = HttpStatus.BAD_REQUEST
}