package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.taggers.Tagset
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TagsetsController : Logging {
    @Operation(
        summary = "List all tagsets",
        description = "List the metadata of all tagsets."
    )
    @CrossOrigin
    @GetMapping(Endpoints.TAGSETS)
    fun getTagsets(): Iterable<Tagset> = Tagset.tagsets.values
}