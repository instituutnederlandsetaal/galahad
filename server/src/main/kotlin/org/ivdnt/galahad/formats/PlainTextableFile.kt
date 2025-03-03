package org.ivdnt.galahad.formats

import java.io.Reader

interface PlainTextableFile {

    fun plainTextReader(): Reader

}