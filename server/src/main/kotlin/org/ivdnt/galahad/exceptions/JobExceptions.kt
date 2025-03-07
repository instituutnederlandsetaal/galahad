package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class JobNotFoundException(job: String) : Exception("Job $job not found. Has the tagger tagged the corpus already?")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class SourceLayerNotATaggerException : Exception("The sourceLayer is not a tagger.")

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class DocumentJobNotFoundException(name: String) : Exception("Document $name has not been tagged yet.")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class MergeNotImplementedException(format: String) : Exception("Merging of $format is not possible.")