package org.ivdnt.galahad.evaluation.confusion

import org.ivdnt.galahad.annotations.AnnotationType

val CONFUSION_TYPES: List<AnnotationType> =
    listOf(AnnotationType.POS, AnnotationType.DEPREL, AnnotationType.UPOS, AnnotationType.NER)