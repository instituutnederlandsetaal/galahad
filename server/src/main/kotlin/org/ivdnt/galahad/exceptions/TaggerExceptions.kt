package org.ivdnt.galahad.exceptions

import org.ivdnt.galahad.annotations.AnnotationType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class TaggerNotFoundException(tagger: String) : Exception("Tagger $tagger not found")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class AnnotationNotSupported(tagger: String, annotation: AnnotationType) :
    Exception("Tagger $tagger does not have annotation type $annotation.")

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
class TaggerNoConnectionException(tagger: String) : Exception("Could not connect to $tagger. Try again later.")