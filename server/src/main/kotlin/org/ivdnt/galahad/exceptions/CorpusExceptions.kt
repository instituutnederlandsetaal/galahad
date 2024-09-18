package org.ivdnt.galahad.exceptions

import java.util.*

class CorpusNotFoundException(
    corpusID: UUID
) : Exception("Corpus with ID $corpusID not found.")

class CorpusNameInvalidException(
    corpusName: String
) : Exception("Corpus name $corpusName is invalid. No newlines and length 3-100.")

class CorpusUnauthorizedException(action: String) : Exception("Unauthorized. $action")