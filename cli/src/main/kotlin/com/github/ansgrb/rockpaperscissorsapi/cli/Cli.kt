package com.github.ansgrb.rockpaperscissorsapi.cli

import com.github.ansgrb.rockpaperscissorsapi.shared.GameMove
import com.github.ansgrb.rockpaperscissorsapi.shared.GameResult
import com.github.ansgrb.rockpaperscissorsapi.shared.JoinRequestPayload
import com.github.ansgrb.rockpaperscissorsapi.shared.Move
import com.github.ansgrb.rockpaperscissorsapi.shared.Player
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

object ApiClient {
	private const val SERVER_BASE_URL = "http://localhost:8080"
	private const val WEBSOCKET_BASE_URL = "ws://localhost:8080"

	// Json configuration instance
	val jsonConfiguration: Json = Json {
		prettyPrint = true
		isLenient = true
		ignoreUnknownKeys = true
		classDiscriminator = "type" // for polymorphic serialization
	}

	private val httpClient = HttpClient(CIO) {
		install(ContentNegotiation) {
			json(jsonConfiguration)
		}
		install(WebSockets)
		install(HttpTimeout) {
			requestTimeoutMillis = 15000
			connectTimeoutMillis = 10000
			socketTimeoutMillis = 10000
		}

	}

	suspend fun joinGame(name: String): String {
		return try {
			// Using JoinRequestPayload to match the documented JSON structure for the request.
			// The server should return a Player object.
			val response: Player = httpClient.post("$SERVER_BASE_URL/join") {
				contentType(ContentType.Application.Json)
				setBody(JoinRequestPayload(name = name)) // id will be null by default
			}.body()
			"Joined successfully! Your Player ID is: ${response.id}. Name: ${response.name}"
		} catch (e: ClientRequestException) {
			"Error joining game: ${e.response.status}. Server message: ${e.response.bodyAsText()}"
		} catch (e: Exception) {
			"Failed to connect or other error joining game: ${e.message}"
		}
	}

	suspend fun listPlayers(): String {
		return try {
			val players: List<Player> = httpClient.get("$SERVER_BASE_URL/players").body()
			if (players.isEmpty()) {
				"No players currently in the game."
			} else {
				"Current players:\n" + players.joinToString("\n") { "  - ID: ${it.id}, Name: ${it.name}" }
			}
		} catch (e: ClientRequestException) {
			"Error listing players: ${e.response.status}. Server message: ${e.response.bodyAsText()}"
		} catch (e: Exception) {
			"Failed to connect or other error listing players: ${e.message}"
		}
	}

	suspend fun playGame(playerId: String, cliEcho: (String) -> Unit) {
		val sessionMutex = Mutex() // To safely manage session state/closing

		try {
			cliEcho("Attempting to connect to WebSocket server for player ID: $playerId")
			httpClient.webSocket(
				method = HttpMethod.Get,
				host = "localhost", // Or your server's host
				port = 8080,       // Or your server's port
				path = "/game?playerId=$playerId"
			) { // 'this' is a DefaultClientWebSocketSession
				cliEcho("Connected to game server! Your ID: $playerId. WebSocket session: $this")
				cliEcho("Type your move (ROCK, PAPER, SCISSORS) and press Enter. Type 'QUIT' to exit.")

				val incomingMessagesJob = launch {
					try {
						for (frame in incoming) { // 'incoming' is from DefaultClientWebSocketSession
							if (!isActive) break
							when (frame) {
								is Frame.Text -> {
									val receivedText = frame.readText()
									try {
										val gameResult = jsonConfiguration.decodeFromString<GameResult>(receivedText)
										cliEcho("\n--- Game Result ---")
										cliEcho("Player 1 ID: ${gameResult.player1Id}, Move: ${gameResult.player1Move ?: "N/A"}")
										cliEcho("Player 2 ID: ${gameResult.player2Id}, Move: ${gameResult.player2Move ?: "N/A"}")
										cliEcho("Winner: ${gameResult.winner ?: "N/A"}")
										cliEcho("-------------------")
									} catch (e: SerializationException) {
										cliEcho("Server (raw): $receivedText (Could not parse as GameResult: ${e.message})")
									} catch (e: Exception) {
										cliEcho("Error processing server message: ${e.message}")
									}
								}
								is Frame.Close -> {
									cliEcho("Server initiated close: ${closeReason.await()}")
									break
								}
								else -> {
									cliEcho("Received other frame type: ${frame.frameType.name}")
								}
							}
						}
					} catch (e: ClosedReceiveChannelException) {
						cliEcho("Connection closed by server (receive channel closed).")
					} catch (e: SerializationException) { // Catching it here for more specific feedback
						cliEcho("Error deserializing message from server: ${e.localizedMessage}")
					}
					catch (e: Exception) {
						cliEcho("Error receiving messages: ${e.localizedMessage ?: "Unknown error"}")
					} finally {
						sessionMutex.withLock { // Ensure one coroutine finalizes
							if (isActive) {
								cliEcho("Incoming message listener trying to close session...")
								close(CloseReason(CloseReason.Codes.NORMAL, "Client listener ending"))
							}
						}
						cliEcho("Incoming message listener stopped.")
					}
				}

				val outgoingMessagesJob = launch {
					try {
						while (isActive && incomingMessagesJob.isActive) { // Check both jobs
							// Using withContext to ensure readlnOrNull is cancellable
							val input = withContext(Dispatchers.IO) {
								print("Your move> ")
								readlnOrNull()
							}?.trim()?.uppercase()

							if (!isActive || !incomingMessagesJob.isActive) break // Re-check after blocking call

							if (input == null || input == "QUIT") {
								cliEcho("Quitting game...")
								break // Exit the sending loop
							}

							val moveEnum: Move? = try {
								Move.valueOf(input)
							} catch (e: IllegalArgumentException) {
								null
							}

							if (moveEnum != null) {
								val gameMove = GameMove(playerId = playerId, move = moveEnum)
								try {
									sendSerialized(gameMove) // 'sendSerialized' is an extension on WebSocketSession
									cliEcho("Sent move: $input")
								} catch (e: Exception) {
									cliEcho("Error sending move: ${e.message}")
									// Consider breaking or specific handling if send fails
								}
							} else {
								cliEcho("Invalid move. Options: ROCK, PAPER, SCISSORS, QUIT.")
							}
						}
					} catch (e: CancellationException) {
						cliEcho("Outgoing messages job cancelled.")
						// Propagate cancellation if needed, or just log
					} catch (e: Exception) {
						cliEcho("Error in input/sending loop: ${e.message}")
					} finally {
						sessionMutex.withLock {
							if (isActive) { // Check if the session is still active before trying to close
								cliEcho("Outgoing message handler trying to close session...")
								close(CloseReason(CloseReason.Codes.NORMAL, "Client quitting"))
							}
						}
						cliEcho("Outgoing message handler stopped.")
					}
				}

				// Wait for both jobs to complete.
				// If one fails or closes the session, the other should ideally terminate.
				try {
					listOf(incomingMessagesJob, outgoingMessagesJob).joinAll()
				} catch (e: Exception) {
					cliEcho("Exception during joinAll: ${e.message}")
				}

				cliEcho("WebSocket session coroutine ending.")
			} // End of httpClient.webSocket block
			cliEcho("WebSocket connection block finished.")

		} catch (e: Exception) {
			cliEcho("Error connecting to WebSocket or during game session setup: ${e.message}")
			e.printStackTrace() // Helpful for debugging connection issues
		} finally {
			cliEcho("Play session ended.")
		}
	}
}

