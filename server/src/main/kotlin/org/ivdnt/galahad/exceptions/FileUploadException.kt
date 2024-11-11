package org.ivdnt.galahad.exceptions

class FileUploadException(
    override val message: String,
) : Exception(message)