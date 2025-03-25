package org.ivdnt.galahad.formats.naf

import org.ivdnt.galahad.annotations.*
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.MergeNotImplementedException
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.formats.InternalFile
import org.ivdnt.galahad.util.getXmlBuilder
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class NafFile(
    override val file: File,
) : InternalFile() {
    override val format: DocumentFormat = DocumentFormat.Naf
    override val reader: AnnotationReader by lazy { NafReader(file) }
}
