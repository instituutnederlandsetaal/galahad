package org.ivdnt.galahad.formats.folia

import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.util.XmlMetadata
import org.ivdnt.galahad.util.insertAfter
import org.ivdnt.galahad.util.nextElementSibling
import org.ivdnt.galahad.util.tagName
import org.w3c.dom.Document
import org.w3c.dom.Node

class FoliaMetadata(
    xml: Document,
    val root: Node,
    val export: DocumentExport
) : XmlMetadata(xml) {
    init {
        val meta = root.getOrCreateChild("metadata")
        val annotations: Node = meta.getOrCreateChild("annotations")

        // We also try to fix some documents with missing annotation definitions.
        // Really this should be the user's problem. But why not.
        annotations.getOrCreateChild("text-annotation")
        annotations.getOrCreateChild("paragraph-annotation")
        annotations.getOrCreateChild("sentence-annotation")
        annotations.getOrCreateChild("token-annotation")

        addAnnotationDefinition(annotations, "lemma")
        addAnnotationDefinition(annotations, "pos")

        // Order matters, <provenance> needs to be directly after annotations
        val provenance: Node
        val nextNonTextSibling = annotations.nextElementSibling()
        if (nextNonTextSibling?.tagName() == "provenance") {
            provenance = nextNonTextSibling
        } else {
            provenance = xml.createElement("provenance")
            annotations.parentNode.insertAfter(provenance, annotations)
        }

        val processor = xml.createElement("processor")
        processor.setAttribute("xml:id", export.tagger.id)
        processor.setAttribute("name", export.tagger.id)
        processor.setAttribute("type", "auto")
        processor.setAttribute("src", "https://github.com/INL/galahad-taggers-dockerized")
        processor.setAttribute("host", "https://galahad.ivdnt.org")
        processor.setAttribute("user", export.user.id)

        provenance.appendChild(processor)
    }

    private fun addAnnotationDefinition(annotations: Node, name: String) {
        val anot = xml.createElement("$name-annotation")
        anot.setAttribute("set", export.tagger.id)
        val annotator = xml.createElement("annotator")
        annotator.setAttribute("processor", export.tagger.id)
        anot.appendChild(annotator)
        annotations.appendChild(anot)
    }
}