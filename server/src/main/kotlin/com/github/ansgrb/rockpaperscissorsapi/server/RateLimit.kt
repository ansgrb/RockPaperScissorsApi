package com.github.ansgrb.rockpaperscissorsapi.server

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import kotlin.time.Duration.Companion.seconds

fun Application.configureRateLimit() {
	install(RateLimit) {
		global {
			rateLimiter(limit = 100, refillPeriod = 60.seconds)

			requestKey { call -> call.request.origin.remoteHost }
		}
	}
}