package dev.ansgrb

import io.ktor.server.application.*

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
	configureSockets()
	configureSerialization()
	configureRouting()
	configureCORS()
}

