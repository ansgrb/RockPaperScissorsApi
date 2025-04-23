package dev.ansgrb

import java.util.concurrent.ConcurrentHashMap

object GameState {
	private val players = ConcurrentHashMap<String, Player>()
	private val gameMoves = ConcurrentHashMap<String, GameMove>()
	private val gameResult: GameResult? = null

	fun addPlayer(player: Player): Boolean {
		if (players.size >= 2) return false // max 2 players
		players[player.id] = player
		return true
	}
}