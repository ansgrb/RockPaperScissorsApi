package dev.ansgrb

import com.mongodb.client.model.Filters
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.websocket.send
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

object GameState {

	// websocket sessions for active players (players that joined the game)
	private val sessions = ConcurrentHashMap<String, WebSocketServerSession>() // this is a map of sessions

	private val players = ConcurrentHashMap<String, Player>()
	private val gameMoves = ConcurrentHashMap<String, GameMove>()
	private var gameResult: GameResult? = null

	// need to make it s suspend function to use MongoDB collection
	suspend fun addPlayer(player: Player): Boolean {

		// check how many players already joined
		val playerCount = try {
			MongoDB.playersCollection.countDocuments()
		} catch (e: Exception) {
			throw RuntimeException("Failed to count players: ${e.message}", e)
		}

		// our limit is two players at a time
		if (playerCount >= 2) return false

		// insert player to MongoDB collection
		try {
			MongoDB.playersCollection.insertOne(player)
		} catch (e: Exception) {
			throw RuntimeException("Failed to insert player: ${e.message}", e)
		}
		return true

//		if (players.size >= 2) return false // max 2 players
//		players[player.id] = player
//		return true
	}

	// add player's move and check if the game can be resolved!
	suspend fun addMove(gameMove: GameMove) {
		try {
			MongoDB.gameMovesCollection.insertOne(gameMove)
			// TODO: check the move in Redis
		} catch (e: Exception) {
			throw RuntimeException("Failed to insert move: ${e.message}", e)
		}
	}

	// retrieve all players in the game
	suspend fun getPlayers(): List<Player> {
		return try {
			MongoDB.playersCollection.find().toList()
		} catch (e: Exception) {
			throw RuntimeException("Failed to retrieve players: ${e.message}", e)
		}
	}

	// retrieve the latest game result
	suspend fun getGameResult(): GameResult? {
		return try {
			MongoDB.gameResultsCollection.find().toList().lastOrNull()
		} catch (e: Exception) {
			throw RuntimeException("Failed to retrieve game result: ${e.message}", e)
		}
	}

	suspend fun resetGame() {
		try {
			MongoDB.forAllPurge()
			sessions.clear()
		} catch (e: Exception) {
			throw RuntimeException("Failed to reset game: ${e.message}", e)
		}
	}

	// add a Websocket session for a player (should we make it suspend?)
	fun addSession(playerId: String, session: WebSocketServerSession) {
		sessions[playerId] = session
//		checkGameResult()
	}

	// to remove the WebSocket session
	fun removeSession(playerId: String) {
		sessions.remove(playerId)
	}

	// the game logic (check if both players have made their moves and determine the winner)
	private suspend fun checkGameResult() {
		val moveCount = MongoDB.gameMovesCollection.countDocuments()
		if (moveCount == 2L) { // both players have made their moves
			val player1 = MongoDB.playersCollection.find().toList().firstOrNull()
			val player2 = MongoDB.playersCollection.find().toList()[1]
			val move1 = MongoDB.gameMovesCollection.find(Filters.eq("playerId", player1?.id)).firstOrNull()?.move
			val move2 = MongoDB.gameMovesCollection.find(Filters.eq("playerId", player2.id)).firstOrNull()?.move

			// determine the winner (winner winner checkin dinner)
			val winner = when {
				move1 == move2 -> "tie"
				move1 == Move.ROCK && move2 == Move.SCISSORS -> player1!!.id
				move1 == Move.PAPER && move2 == Move.ROCK -> player1!!.id
				move1 == Move.SCISSORS && move2 == Move.PAPER -> player1!!.id
				else -> player2.id
			}

			// store the result in MongoDB
			val gameResult = GameResult(
				player1Id = player1!!.id,
				player2Id = player2.id,
				player1Move = move1,
				player2Move = move2,
				winner = winner,
			)
			MongoDB.gameResultsCollection.insertOne(gameResult)

			// cast the result to all connected Websocket clients
			sessions.values.forEach { session ->
				try {
					session.send(Json.encodeToString(gameResult))
				} catch (e: Exception) {
					println("Failed to send game result to client: ${e.message}")
				}
//				sessions.clear()
			}

			// clear moves for the next round
			MongoDB.gameMovesCollection.deleteMany(Filters.empty())
		}
	}
}