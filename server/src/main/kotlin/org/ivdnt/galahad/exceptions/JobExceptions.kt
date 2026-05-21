package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

// Note the difference between tagger not found and job not found.
// A tagger may exist, but may not have run yet.
@ResponseStatus(value = HttpStatus.NOT_FOUND)
class JobNotFoundException(job: String) : Exception("Job $job not found.")

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class DocumentJobNotFoundException(name: String) :
    Exception("Document $name has not been tagged yet.")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class MergeNotImplementedException(format: String) :
    Exception("Merging of $format is not possible.")
