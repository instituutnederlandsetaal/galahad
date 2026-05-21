package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.layers.CorpusLayerMetadata
import org.ivdnt.galahad.web.service.LayerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class LayerController(private val layerService: LayerService) : Logging {

    @Operation(
        summary = "List all layer metadata",
        description = "List all layer metadata in a corpus.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "CorpusLayerMetadata of all layers in the corpus.",
    )
    @ApiResponse(
        responseCode = "403",
        description = "User needs read-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @GetMapping(Endpoints.Layers.BASE)
    fun getLayers(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID
    ): List<CorpusLayerMetadata> = layerService.readAll(corpus)

    @Operation(
        summary = "Get single layer metadata",
        description = "Get the metadata of a single layer.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Layer metadata.",
        content = [Content(mediaType = "text/plain,*/*")],
    )
    @ApiResponse(
        responseCode = "403",
        description = "User needs read-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or layer not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @GetMapping(Endpoints.Layers.LAYER)
    fun getLayer(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
    ): CorpusLayerMetadata = layerService.readOrThrow(corpus, layer)

    @Operation(summary = "Delete single layer", description = "Delete a layer and its jobs.")
    @ApiResponse(responseCode = "204", description = "Layer deleted.")
    @ApiResponse(
        responseCode = "403",
        description = "User needs write-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or layer not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @CrossOrigin
    @DeleteMapping(Endpoints.Layers.LAYER)
    fun deleteLayer(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
    ): ResponseEntity<String> {
        layerService.deleteOrThrow(corpus, layer)
        return ResponseEntity.noContent().build()
    }
}
