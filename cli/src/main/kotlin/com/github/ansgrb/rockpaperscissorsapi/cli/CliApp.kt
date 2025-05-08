package com.github.ansgrb.rockpaperscissorsapi.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking

class CliApp : CliktCommand() {
	init {
		subcommands(LoginCommand(), JoinCommand(), ListPlayersCommand(), PlayCommand())
	}

	override fun run() {
		echo("Welcome to Rock Paper Scissors CLI!")
		echo("Use a subcommand: 'login <name>', 'join', 'list-players', or 'play'. Try 'rps <subcommand> --help'.")
	}
}

class LoginCommand : CliktCommand(name = "login") {
	private val name: String by argument(help = "Your player name")
	override fun run() = runBlocking { echo(ApiClient.login(name)) }
}

class JoinCommand : CliktCommand() {
	override fun run() = runBlocking { echo(ApiClient.joinGame()) }
}

class ListPlayersCommand : CliktCommand(name = "list-players") {
	override fun run() = runBlocking { echo(ApiClient.listPlayers()) }
}

class PlayCommand : CliktCommand(name = "play") {
	override fun run() {
		// the `ApiClient` will use its internally stored currentPlayerId
		echo("Starting interactive game session...")
		runBlocking { ApiClient.playGame { message -> this@PlayCommand.echo(message) } }
	}
}
