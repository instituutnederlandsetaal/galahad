package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class ApplicationController : Logging {
    @Autowired
    private val request: HttpServletRequest? = null

    @Operation(
        summary = "Get version information",
        description = "Get version information and GitHub build information and commit version.",
        responses = [
            ApiResponse(
                description = "Version information."
            )
        ]
    )
    @CrossOrigin
    @GetMapping(VERSION_URL)
    fun getVersion(): Map<String, String> {
        return Config.Companion.galahadVersionYaml().entries.associate { it.key.toString() to it.value.toString() }
    }

    @Hidden
    @CrossOrigin
    @GetMapping(BASE_URL)
    fun getApplication(): ResponseEntity<Void> {
        // Since we have nothing to show at this URL, we redirect to the API UI instead
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(request?.contextPath + SWAGGER_API_URL))
            .build()
    }

    @Operation(
        summary = "Get user information",
        description = "Get the username and whether the user is an admin.",
        responses = [
            ApiResponse(
                description = "User information."
            )
        ]
    )
    @CrossOrigin
    @GetMapping("/user")
    fun getUser(): User {
        return User.Companion.fromRequest(request)
    }
}