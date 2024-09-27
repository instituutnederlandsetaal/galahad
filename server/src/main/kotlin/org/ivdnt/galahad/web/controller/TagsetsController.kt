package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.TAGSETS_URL
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.tagset.Tagset
import org.ivdnt.galahad.tagset.TagsetStore
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TagsetsController : Logging {

    val tagsets = TagsetStore()

    @Operation(
        summary = "List all tagsets",
        description = "List the metadata of all tagsets."
    )
    @CrossOrigin
    @GetMapping(TAGSETS_URL)
    fun getTagsets(): Set<Tagset> {
        return tagsets.tagsets
    }

    @Operation(
        summary = "Get single tagset",
        description = "Get the metadata of a single tagset.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Metadata of the requested tagset.",
    )
    @ApiResponse(
        responseCode = "404",
        description = "The tagset was not found.",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))]
    )
    @CrossOrigin
    @GetMapping("$TAGSETS_URL/{tagset}")
    fun getTagset(
        @PathVariable("tagset") @Parameter(description = "Tagset identifier") identifier: String,
    ): Tagset {
        return tagsets.getOrThrow(identifier)
    }

}