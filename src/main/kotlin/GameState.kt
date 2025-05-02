package dev.ansgrb

import io.ktor.server.websocket.WebSocketServerSession
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

	fun addMove(gameMove: GameMove) {
		gameMoves[gameMove.playerId.id] = gameMove
		checkGameResult()
	}

	fun getPlayers(): List<Player> = players.values.toList()

	fun getGameResult(): GameResult? = gameResult

	fun resetGame() {
		players.clear()
		gameMoves.clear()
		gameResult = null
	}

	private fun checkGameResult() {
		if (gameMoves.size == 2) {
			val player1 = players.values.first()
			val player2 = players.values.last()
			val move1 = gameMoves[player1.id]?.move
			val move2 = gameMoves[player2.id]?.move

			val winner = when {
				move1 == move2 -> "tie"
				move1 == Move.ROCK && move2 == Move.SCISSORS -> player1.id
				move1 == Move.PAPER && move2 == Move.ROCK -> player1.id
				move1 == Move.SCISSORS && move2 == Move.PAPER -> player1.id
				else -> player2.id
			}

			gameResult = GameResult(
				player1 = player1,
				player2 = player2,
				player1Move = move1,
				player2Move = move2,
				winner = winner,
			)
		}
	}
}