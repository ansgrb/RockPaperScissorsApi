package com.github.ansgrb.rockpaperscissorsapi.server

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.github.ansgrb.rockpaperscissorsapi.shared.GameMove
import com.github.ansgrb.rockpaperscissorsapi.shared.GameResult
import com.github.ansgrb.rockpaperscissorsapi.shared.Move
import com.github.ansgrb.rockpaperscissorsapi.shared.Player
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

// Consider injecting ApplicationEnvironment for logging if needed elsewhere
// object GameState(private val log: io.ktor.util.logging.Logger) {
object GameState { // Keeping it simple for now

	// websocket sessions for active players (players that joined the game)
	private val sessions = ConcurrentHashMap<String, WebSocketServerSession>() // this is a map of sessions


	// --- Player Management ---
	suspend fun addPlayer(player: Player): Boolean {
		// Use countDocuments with a filter if needed, or handle potential race conditions if strict < 2 is required
		val playerCount = try {
			MongoDB.playersCollection.countDocuments()
		} catch (e: Exception) {
			// log.error("Failed to count players", e)
			throw RuntimeException("Failed to count players: ${e.message}", e)
		}

		if (playerCount >= 2) return false

		try {
			MongoDB.playersCollection.insertOne(player)
		} catch (e: Exception) {
			// log.error("Failed to insert player ${player.id}", e)
			throw RuntimeException("Failed to insert player: ${e.message}", e)
		}
		return true
	}

	suspend fun getPlayers(): List<Player> {
		return try {
			MongoDB.playersCollection.find().toList()
		} catch (e: Exception) {
			// log.error("Failed to retrieve players", e)
			throw RuntimeException("Failed to retrieve players: ${e.message}", e)
		}
	}

	// --- Session Management ---
	fun addSession(playerId: String, session: WebSocketServerSession) {
		// log.info("Adding session for player $playerId")
		sessions[playerId] = session
		// Removed checkGameResult() call - should happen on move, not connection
	}

	fun removeSession(playerId: String) {
		// log.info("Removing session for player $playerId")
		sessions.remove(playerId)
	}

	// --- Game Logic ---
	suspend fun addMove(gameMove: GameMove) {
		try {
			MongoDB.gameMovesCollection.insertOne(gameMove)
			// log.info("Added move for player ${gameMove.playerId}: ${gameMove.move}")
			// *** CRITICAL: Call checkGameResult after adding the move ***
			checkGameResult()
		} catch (e: Exception) {
			// log.error("Failed to insert move for player ${gameMove.playerId}", e)
			throw RuntimeException("Failed to insert move: ${e.message}", e)
		}
	}

	suspend fun getGameResult(): GameResult? {
		return try {
			// fetch the most recently inserted result (consider adding a timestamp and sorting if needed)
			MongoDB.gameResultsCollection.find().sort(Sorts.descending("_id")).firstOrNull()
		} catch (e: Exception) {
			// log.error("Failed to retrieve game result", e)
			throw RuntimeException("Failed to retrieve game result: ${e.message}", e)
		}
	}

	suspend fun resetGame() {
		try {
			// log.warn("Resetting game state")
			MongoDB.forAllPurge() // ensure this clears players, moves, and results collections
			sessions.values.forEach { session ->
				try {
					// optionally notify clients about the reset
					// session.close(CloseReason(CloseReason.Codes.NORMAL, "Game reset by server"))
				} catch (_: Exception) { /* Ignore errors closing already closed sessions */ }
			}
			sessions.clear()
		} catch (e: Exception) {
			// log.error("Failed to reset game", e)
			throw RuntimeException("Failed to reset game: ${e.message}", e)
		}
	}

	private suspend fun checkGameResult() {
		val moveCount = try {
			MongoDB.gameMovesCollection.countDocuments()
		} catch (e: Exception) {
			// log.error("Failed to count moves during checkGameResult", e)
			return // cannot proceed if DB fails
		}

		// log.debug("Checking game result. Move count: $moveCount")

		if (moveCount == 2L) { // both players have made their moves
			// log.info("Both players have moved. Calculating result...")
			try {
				// fetch players - Potential issue: Relies on insertion order? safer to fetch both?
				val playersList = MongoDB.playersCollection.find().limit(2).toList()
				if (playersList.size < 2) {
					// log.error("Less than 2 players found in collection during result calculation.")
					return // need two players
				}
				val player1 = playersList[0]
				val player2 = playersList[1]

				// fetch moves using player IDs
				val move1Data = MongoDB.gameMovesCollection.find(Filters.eq("playerId", player1.id)).firstOrNull()
				val move2Data = MongoDB.gameMovesCollection.find(Filters.eq("playerId", player2.id)).firstOrNull()

				if (move1Data == null || move2Data == null) {
					// log.error("Could not find moves for both players (P1: ${player1.id}, P2: ${player2.id})")
					return // need both moves
				}
				val move1 = move1Data.move
				val move2 = move2Data.move

				// determine the winner
				val winnerId = when {
					move1 == move2 -> "tie"
					(move1 == Move.ROCK && move2 == Move.SCISSORS) ||
					(move1 == Move.PAPER && move2 == Move.ROCK) ||
					(move1 == Move.SCISSORS && move2 == Move.PAPER) -> player1.id
					else -> player2.id
				}
				// log.info("Game determined. Winner: $winnerId")

				// store the result in MongoDB
				val gameResult = GameResult(
					player1Id = player1.id,
					player2Id = player2.id,
					player1Move = move1,
					player2Move = move2,
					winner = winnerId,
				)
				MongoDB.gameResultsCollection.insertOne(gameResult)
				// log.info("Game result saved to DB: $gameResult")

				// broadcast the result to all connected WebSocket clients
				val resultJson = Json.encodeToString(gameResult)
				sessions.forEach { (playerId, session) ->
					try {
						// ensure session is still open before sending
						if (session.isActive) {
							session.send(Frame.Text(resultJson))
							// log.info("Sent result to player $playerId")
						} else {
							// log.warn("Session for player $playerId was inactive. Could not send result.")
							// consider removing inactive sessions proactively
							sessions.remove(playerId)
						}
					} catch (e: Exception) {
						// log.error("Failed to send game result to player $playerId", e)
						// consider removing session if send fails repeatedly
						sessions.remove(playerId)
					}
				}

				// clear moves for the next round
				// consider delaying this or doing it before the next game starts
				MongoDB.gameMovesCollection.deleteMany(Filters.empty())
				// log.info("Cleared game moves collection.")

				// optional: Clear players collection if game instance is truly over
				// MongoDB.playersCollection.deleteMany(Filters.empty())
				// log.info("Cleared players collection.")

			} catch (e: Exception) {
				// log.error("Error during checkGameResult calculation/broadcast", e)
			}
		}
	}
}