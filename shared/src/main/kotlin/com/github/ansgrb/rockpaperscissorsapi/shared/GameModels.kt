package com.github.ansgrb.rockpaperscissorsapi.shared

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
	val id: String = UUID.randomUUID().toString(),
	val playerId: String,
	val move: Move,
)

@Serializable
data class GameResult(
	val id: String = UUID.randomUUID().toString(),
	val player1Id: String,
	val player2Id: String,
	val player1Move: Move?,
	val player2Move: Move?,
	val winner: String?,
)

@Serializable
data class JoinRequestPayload(val id: String? = null, val name: String)

@Serializable
data class LoginResponse(val token: String, val playerId: String, val name: String)
