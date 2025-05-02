package dev.ansgrb

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Player(
	val id: String = UUID.randomUUID().toString(),
	val name: String,
)

@Serializable
enum class Move {
	ROCK,
	PAPER,
	SCISSORS,
}

@Serializable
data class GameMove(
	val playerId: Player,
	val move: Move,
)

// raw data from the client **acts as a temporary storage**
@Serializable
data class RawGameMove(
	val playerId: String = UUID.randomUUID().toString(),
	val move: Move,
)

@Serializable
data class GameResult(
	val id: String = UUID.randomUUID().toString(),
	val player1: Player,
	val player2: Player?,
	val player1Move: Move?,
	val player2Move: Move?,
	val winner: String?,
)