package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus

class JobNotFoundException(
    identifier: String
) : Exception("Job $identifier not found. Has the tagger tagged the corpus already?"), RESTException {
    override val statusCode: HttpStatus = HttpStatus.NOT_FOUND
}

class SourceLayerNotATaggerException : Exception("The sourceLayer is not a tagger."), RESTException {
    override val statusCode: HttpStatus = HttpStatus.BAD_REQUEST
}

class DocumentJobNotFoundException(
    name: String
) : Exception("Document $name has not been tagged yet."), RESTException {
    override val statusCode: HttpStatus = HttpStatus.NOT_FOUND
}
