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
		post("/join") {
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

			val player = GameState.getPlayers().find { it.id == playerId }
			if (player == null) {
				close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid player ID"))
				return@webSocket
			}

			connectionMap[playerId] = this
			try {
				for (frame in incoming) {
					if (frame is Frame.Text) {

						val rawMove = Json.decodeFromString<RawGameMove>(frame.readText())

						if (rawMove.playerId != playerId) {
							continue
						}

						val gameMove = GameMove(playerId = player, move = rawMove.move)

						GameState.addMove(gameMove)
						val result = GameState.getGameResult()
						if (result != null) {
							connectionMap.values.forEach { connection ->
								connection.send(Json.encodeToString(result))
							}
							GameState.resetGame()
							connectionMap.clear()
							break
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













