package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.exceptions.InvalidAnnotationException

enum class Annotation(@JsonValue val value: String) {
    TOKEN("token"),
    LEMMA("lemma"),
    POS("pos"),
    UPOS("upos"),
    HEAD("head"),
    DEPREL("deprel"),
    NER("ner");

    override fun toString(): String = value

    companion object {
        @JsonCreator
        fun fromString(s: String): Annotation =
            entries.firstOrNull { it.value == s.lowercase() } ?: throw InvalidAnnotationException(
                "Invalid annotation type $s, valid types are ${entries.map { it.value }}"
            )

        fun order(other: Iterable<Annotation>): List<Annotation> {
            return entries.filter { it in other }
        }
        fun order(other: Array<Annotation>): List<Annotation> {
            return entries.filter { it in other }
        }
    }
}

