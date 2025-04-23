package dev.ansgrb

import java.util.concurrent.ConcurrentHashMap

object GameState {
	private val players = ConcurrentHashMap<String, Player>()
	private val gameMoves = ConcurrentHashMap<String, GameMove>()
	private var gameResult: GameResult? = null

	fun addPlayer(player: Player): Boolean {
		if (players.size >= 2) return false // max 2 players
		players[player.id] = player
		return true
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