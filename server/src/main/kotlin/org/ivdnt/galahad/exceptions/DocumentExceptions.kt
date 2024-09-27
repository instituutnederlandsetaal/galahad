package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class DocumentNotFoundException(document: String) : Exception("Document $document not found.")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class DocumentInvalidException(document: String, details: String? = null) : Exception("Document $document is invalid. ${details ?: ""}")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidDocumentFormatException(details: String) : Exception(details)