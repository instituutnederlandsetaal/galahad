package org.ivdnt.galahad.formats

import org.ivdnt.galahad.annotations.AnnotationType
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.documents.Document
import org.ivdnt.galahad.corpora.documents.DocumentFormat
import org.ivdnt.galahad.corpora.jobs.Job
import org.ivdnt.galahad.taggers.Tagger

open class CorpusTransformMetadata(
    val corpus: Corpus,
    val job: Job,
    val user: User,
    val targetFormat: DocumentFormat,
) {

    fun documentMetadata(document: String): DocumentTransformMetadata {
        return DocumentTransformMetadata(
            corpus = corpus,
            job = job,
            document = corpus.documents.readOrThrow(document),
            user = user,
            targetFormat = targetFormat,
        )
    }
}

class DocumentTransformMetadata(
    val corpus: Corpus,
    val job: Job,
    val document: Document,
    val user: User,
    val targetFormat: DocumentFormat,
) {

    val layer: Layer = job.layer(document)

    val tagger: Tagger = Tagger.readOrThrow(job.name, corpus)

    val plainText: String
        get() = document.plaintext

    fun convertLayerToPosHead() {
        for (i in layer.terms.indices) {
            val t = layer.terms[i]
            layer.terms[i] = Term(
                lemma = t.lemma, pos = t.annotationHead(AnnotationType.POS), targets = t.targets
            )
        }
    }
}
