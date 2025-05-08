package com.github.ansgrb.rockpaperscissorsapi.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking

class CliApp : CliktCommand() {
	init {
		subcommands(JoinCommand(), ListPlayersCommand(), PlayCommand())
	}

	override fun run() {
		echo("Welcome to Rock Paper Scissors CLI!")
		echo("Use a subcommand: 'join', 'list-players', or 'play'. Try 'rps <subcommand> --help'.")

	}
}

class JoinCommand : CliktCommand() {
	private val name: String by argument(help = "Your player name")
	override fun run() = runBlocking { echo(ApiClient.joinGame(name)) }
}

class ListPlayersCommand : CliktCommand(name = "list-players") {
	override fun run() = runBlocking { echo(ApiClient.listPlayers()) }
}

class PlayCommand : CliktCommand() {
	private val playerId: String by option("-p", "--player-id", help = "Your Player ID").required()
	override fun run() {
		echo("Starting interactive game session for Player ID: $playerId")
		runBlocking { ApiClient.playGame(playerId) { message -> this@PlayCommand.echo(message) } }
	}
}
