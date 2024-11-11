package org.ivdnt.galahad.data.layer

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.exceptions.InvalidAnnotationException

enum class AnnotationType(@JsonValue val value: String) {
    TOKEN("token"), LEMMA("lemma"), POS("pos"),
    UPOS("upos"), HEAD("head"), DEPREL("deprel"),
    MISC("misc"), ID("id"), NER("named-entity");

    companion object {
        fun fromString(s: String): AnnotationType = values().firstOrNull { it.value == s.lowercase() } ?: throw InvalidAnnotationException(
            "Invalid annotation type $s, valid types are ${values().map { it.value }}"
        )

        fun fromString(s: List<String>): List<AnnotationType> = s.map { fromString(it) }
    }
}

typealias Annotations = Map<AnnotationType, String?>

val Annotations.token: String
    get() = this[AnnotationType.TOKEN]!!

val Annotations.lemma: String?
    get() = this[AnnotationType.LEMMA]

val Annotations.pos: String?
    get() = this[AnnotationType.POS]

val Annotations.upos: String?
    get() = this[AnnotationType.UPOS]

val Annotations.head: String?
    get() = this[AnnotationType.HEAD]

val Annotations.deprel: String?
    get() = this[AnnotationType.DEPREL]

val Annotations.misc: String?
    get() = this[AnnotationType.MISC]

val Annotations.id: String?
    get() = this[AnnotationType.ID]

