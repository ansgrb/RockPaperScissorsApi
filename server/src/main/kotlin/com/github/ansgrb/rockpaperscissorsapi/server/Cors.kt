package com.github.ansgrb.rockpaperscissorsapi.server

import io.ktor.http.HttpMethod
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.install

fun Application.configureCORS() {
	install(CORS) {
//		anyHost()
		allowHost("ansgrb.github.io", schemes = listOf("https"))
		allowMethod(HttpMethod.Get)
		allowMethod(HttpMethod.Post)
		allowMethod(HttpMethod.Options)
		allowHeader(HttpHeaders.ContentType)
		allowHeader(HttpHeaders.Authorization)
		allowCredentials = true
		allowHeaders { true } // or allow all headers
	}
}