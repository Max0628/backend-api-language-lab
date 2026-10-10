package com.maxchauo.backendapilanguagelab.jobs

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory

/** Registers jobs route and standard API error handling. */
fun Application.configureRouting(service: JobsService) {
    routing {
        get("/jobs") {
            call.respond(service.list(call.request.queryParameters).map(Job::toResponse))
        }
    }
}

/** Configures the API error response format. */
fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("JobsApi")
    install(StatusPages) {
        exception<InvalidParameterException> { call, exception ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ErrorDetails("INVALID_PARAMETER", exception.message ?: "invalid parameter")),
            )
        }
        exception<Throwable> { call, exception ->
            logger.error("List jobs failed", exception)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(ErrorDetails("INTERNAL_SERVER_ERROR", "failed to query jobs")),
            )
        }
    }
}

/** Standard API error response. */
@kotlinx.serialization.Serializable
data class ErrorResponse(val error: ErrorDetails)

/** Machine-readable API error details. */
@kotlinx.serialization.Serializable
data class ErrorDetails(val code: String, val message: String)
