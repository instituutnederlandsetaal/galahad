package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.taggers.Tagset
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TagsetsController : Logging {
    @Operation(summary = "List all tagsets", description = "List the metadata of all tagsets.")
    @CrossOrigin
    @GetMapping(Endpoints.Tagsets.BASE)
    fun getTagsets(): Iterable<Tagset> = Tagset.tagsets.values

    @Operation(summary = "Get tagset by id", description = "Metadata of the tagset.")
    @CrossOrigin
    @GetMapping(Endpoints.Tagsets.TAGSET)
    fun getTagset(@PathVariable @Parameter(description = "Tagset id") id: String): Tagset =
        Tagset.readOrThrow(id)
}
