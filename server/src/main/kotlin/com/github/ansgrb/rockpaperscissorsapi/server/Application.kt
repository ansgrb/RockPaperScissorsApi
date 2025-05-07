package com.github.ansgrb.rockpaperscissorsapi.server

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
	EngineMain.main(args)
}

fun Application.module() {

	configureErrorHandler()
	initMongoDB() // initialize mongo
	configureAuth()
	configureSockets()
	configureSerialization()
	configureRouting()
	configureCORS()
	configureRateLimit()

}

