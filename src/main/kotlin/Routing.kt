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
import kotlinx.serialization.json.Json
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
		// WebSocket /game -- Handle moves and broadcast results
		webSocket("/game") {
			val playerId = call.parameters["playerId"] ?: run {
				close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Player ID required"))
				return@webSocket
			}
			if (GameState.getPlayers().none { it.id == playerId }) {
				close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid player ID"))
				return@webSocket
			}
			connectionMap[playerId] = this
			try {
				for (frame in incoming) {
					if (frame is Frame.Text) {
						val move = Json.decodeFromString<GameMove>(frame.readText())
						if (move.playerId.id != playerId) {
							GameState.addMove(move)
							val result = GameState.getGameResult()
							if (result != null) {
								// send result to all players :)
								connectionMap.values.forEach { connection ->
									connection.send(Frame.Text(Json.encodeToString(result)))
								}
								GameState.resetGame() // reset game state
								connectionMap.clear() // clear connection map
								break
							}
						}

					}
				}
			} catch (e: Exception) {
				println("WebSocket error: ${e.message}") // bark if something goes wrong
			} finally {
				connectionMap.remove(playerId)
			}
		}
	}
}













