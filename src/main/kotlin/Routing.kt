package dev.ansgrb

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

fun Application.configureRouting() {
	val connectionMap = mutableMapOf<String, DefaultWebSocketServerSession>()

	routing {
		// POST /joit -- join the game!
		get("/join") {
			val player = call.receive<Player>()
			val playerId = UUID.randomUUID().toString()
			val newPlayer = player.copy(id = playerId)
			if (GameState.addPlayer(newPlayer)) {
				call.respond(HttpStatusCode.Created, newPlayer)
			} else {
				call.respond(HttpStatusCode.Conflict, "Game is full")
			}
		}
		// GET /players -- list players
		get("/players") {
			call.respond(GameState.getPlayers())
		}
	}
}
