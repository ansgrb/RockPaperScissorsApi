package dev.ansgrb

import io.ktor.server.application.*

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

	initMongoDB() // initialize mongo
	configureAuth()
	configureSockets()
	configureSerialization()
	configureRouting()
	configureCORS()

}

