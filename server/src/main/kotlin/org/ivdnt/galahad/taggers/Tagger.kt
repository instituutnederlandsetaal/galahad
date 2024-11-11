package org.ivdnt.galahad.taggers

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.app.JSONable
import org.ivdnt.galahad.data.layer.AnnotationType
import org.ivdnt.galahad.data.layer.Annotations

class Tagger (
    // The id should be equal to the filename
    // i.e. mytagger.yaml should have id 'mytagger'
    // This ought te be set when loading from file
    // This name will be used as hostname
    // So can only contain certain characters
    @JsonProperty("id") var id: String = "",
    @JsonProperty("description") var description: String = "",
    @JsonProperty("tagset") var tagset: String? = null,
    @JsonProperty("eraFrom") var eraFrom: Int = 0,
    @JsonProperty("eraTo") var eraTo: Int = 0,
    @JsonProperty("produces") var produces: Set<String> = setOf(),
    @JsonProperty("model") var model: LinkItem = LinkItem(),
    @JsonProperty("software") var software: LinkItem = LinkItem(),
    @JsonProperty("dataset") var dataset: LinkItem = LinkItem(),
    @JsonProperty("trainedBy") var trainedBy: String = "",
    @JsonProperty("date") var date: String = "",
) : JSONable {
    @JsonIgnore
    var version: String = ""
    @JsonIgnore
    var devport: Int? = 0
    @get:JsonIgnore
    val annotationTypes: List<AnnotationType>
        get() = AnnotationType.fromString(produces.toList())

    class LinkItem (
        @JsonProperty("name") var name: String = "",
        @JsonProperty("href") var href: String = ""
    )
}