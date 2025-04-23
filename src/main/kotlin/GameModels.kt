package dev.ansgrb

import kotlinx.serialization.Serializable

@Serializable
data class Player(
	val id: String,
	val name: String,
)

@Serializable
enum class Move {
	ROCK,
	PAPER,
	SCISSORS,
}

@Serializable
data class GameResult(
	val player1: Player,
	val player2: Player?,
	val player1Move: Move?,
	val player2Move: Move?,
	val winner: String?,
)