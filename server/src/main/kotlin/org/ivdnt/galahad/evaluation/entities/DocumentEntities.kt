package org.ivdnt.galahad.evaluation.entities

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import kotlin.collections.map

object DocumentEntities {
    fun fromLayer(layer: Layer): List<Entity> =
        layer.documents.flatMap {
            it.paragraphs.flatMap {
                it.sentences.flatMap { sent ->
                    sent.spans?.get(Annotation.NER)?.map { span -> span.value to span.indices.map { sent.terms[it] }.joinToString("") { it.token + it.space } } ?: emptyList()
                }
            }
        }.groupBy{ it }.mapValues { it.value.size }.map{
            Entity(
                label = it.key.first,
                form = it.key.second,
                count = it.value,
            )
        }

    class Entity(
        val label: String,
        val form: String,
        val count: Int,
    )
}