package org.ivdnt.galahad.exceptions

import org.ivdnt.galahad.annotations.Annotation.entries
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidAnnotationException(annotation: String) :
    Exception("Invalid annotation $annotation, valid values are $entries")

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class LayerNotFoundException(layer: String) : Exception("Layer $layer not found.")
