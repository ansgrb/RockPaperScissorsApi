package dev.ansgrb

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
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
		// login endpoint to generate a JWT token
		post("/login") {
			val player = call.receive<Player>()
			val user = User(id = UUID.randomUUID().toString(), name = player.name)
			val token = generateToken(user)
			call.respond(mapOf("token" to token, "playerId" to user.id, "name" to user.name))
		}

		// secure the /join endpoint with JWT authentication
		authenticate("auth-jwt") {
			post("/join") {
				val principal = call.principal<JWTPrincipal>()
				val playerId = principal?.payload?.getClaim("id")?.asString() ?: throw IllegalArgumentException("Invalid token")
				val name = principal.payload.getClaim("name").asString()
				val newPlayer = Player(id = playerId, name = name)

				// add the player to the game and respond with the player object or 409 Conflict if the game is full.
				if (GameState.addPlayer(newPlayer)) {
					call.respond(HttpStatusCode.Created, newPlayer)
				} else {
					call.respond(HttpStatusCode.Conflict, "Game is full")
				}
			}
		}

		// WebSocket /game -- Handle moves and broadcast results
		webSocket("/game") {
			val principal = call.principal<JWTPrincipal>()
			val playerId = principal?.payload?.getClaim("id")?.asString() ?: run {
				close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Invalid token"))
				return@webSocket
			}
			GameState.addSession(playerId, this)
			try {
				for (frame in incoming) {
					if (frame is Frame.Text) {
						val move = Json.decodeFromString<GameMove>(frame.readText())
						if (move.playerId == playerId) {
							GameState.addMove(move)
						}
					}
				}
			} catch (e: Exception) {
				call.application.environment.log.error("WebSocket error for player $playerId", e)
			} finally {
				GameState.removeSession(playerId)
			}
		}

		// GET /players -- list players (public endpoint to get all players)
		get("/players") {
			call.respond(GameState.getPlayers())
		}

		// GET /result -- get the game result (public endpoint to get the game result)
		get("/result") {
			val result = GameState.getGameResult()
			if (result == null) {
				call.respond(HttpStatusCode.NotFound, "No game result yet")
			} else {
				call.respond(result)
			}
//			GameState.resetGame()
		}

		 // GET /reset -- reset the game (public endpoint to reset the game)
		get("/reset") {
			GameState.resetGame()
			call.respond(HttpStatusCode.OK, "Game reset")
		}
	}
}














