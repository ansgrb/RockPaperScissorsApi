package com.github.ansgrb.rockpaperscissorsapi.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import javax.naming.AuthenticationException

fun Application.configureErrorHandler() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception caught: ${cause.localizedMessage}", cause)
            call.respondText(
                text = "500: Internal Server Error. Please contact administrator.",
                status = HttpStatusCode.InternalServerError
            )
        }
        exception<AuthenticationException> { call, cause ->
            call.application.log.warn("Authentication failed: ${cause.localizedMessage}")
            call.respond(HttpStatusCode.Unauthorized, "Authentication Failed")
        }
        exception<BadRequestException> { call, cause ->
            call.application.log.info("Bad request: ${cause.localizedMessage}")
            call.respond(HttpStatusCode.BadRequest, cause.localizedMessage)
        }
    }
}
