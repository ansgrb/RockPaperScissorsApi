package com.github.ansgrb.rockpaperscissorsapi.server

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.github.ansgrb.rockpaperscissorsapi.shared.GameMove
import com.github.ansgrb.rockpaperscissorsapi.shared.GameResult
import com.github.ansgrb.rockpaperscissorsapi.shared.Player
import io.ktor.server.application.Application
import kotlinx.coroutines.runBlocking

object MongoDB {

	private val client: MongoClient = try {
		val connectionString = System.getenv("MONGO_URL") ?: "mongodb://root:russia@localhost:27017/rps-mongo_db?authSource=admin"
		if (System.getProperty("test") == "true") {
			throw Exception("Test mode - skipping MongoDB connection")
		}
		MongoClient.create(connectionString)
	} catch (e: Exception) {
		throw RuntimeException("Failed to connect to MongoDB: ${e.message}", e)
	}

	private val database: MongoDatabase = client.getDatabase("game")

	val playersCollection = database.getCollection<Player>("players")
	val gameMovesCollection = database.getCollection<GameMove>("gameMoves")
	val gameResultsCollection = database.getCollection<GameResult>("gameResults")

	suspend fun init() {
		if (System.getProperty("test") == "true") {
			println("Test mode - skipping MongoDB initialization")
			return
		}


		try {
			playersCollection.createIndex(Indexes.ascending("id"))
			gameMovesCollection.createIndex(Indexes.ascending("playerId"))
			gameResultsCollection.createIndex(Indexes.ascending("winner"))
			println("MongoDB collections initialized")

		} catch (e: Exception) {
			throw RuntimeException("Failed to initialize MongoDB collections: ${e.message}", e)
		}
	}

	suspend fun forAllPurge() {
		try {
			playersCollection.deleteMany(Filters.empty())
			gameMovesCollection.deleteMany(Filters.empty())
			gameResultsCollection.deleteMany(Filters.empty())
			println("MongoDB collections purged")
		} catch (e: Exception) {
			throw RuntimeException("Failed to purge MongoDB collections: ${e.message}", e)
		}
	}
}

// we added this function to be able to run it from tests and to initialize MongoDB on startup with ktor
fun Application.initMongoDB() {
	runBlocking {
		MongoDB.init()
	}
}