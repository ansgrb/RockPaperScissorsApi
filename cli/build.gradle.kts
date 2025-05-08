val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
	kotlin("jvm")
	application
}

dependencies {
	implementation(project(":shared"))

	implementation("com.github.ajalt.clikt:clikt:5.0.3")
	implementation("com.github.ajalt.clikt:clikt-markdown:5.0.3")
	implementation("com.github.ajalt.mordant:mordant:3.0.2")
	implementation("com.github.ajalt.mordant:mordant-coroutines:3.0.2")
	implementation("com.github.ajalt.mordant:mordant-markdown:3.0.2")
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
	implementation("io.ktor:ktor-client-core:${ktorVersion}")
	implementation("io.ktor:ktor-client-cio:${ktorVersion}")
//	implementation("io.ktor:ktor-server-content-negotiation:${ktorVersion}")
	implementation("io.ktor:ktor-client-content-negotiation:${ktorVersion}")
	implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
	implementation("io.ktor:ktor-client-websockets:${ktorVersion}")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
//	implementation("org.slf4j:slikt-simple:2.0.13")
}

application {
	mainClass.set("com.github.ansgrb.rockpaperscissorsapi.cli.MainKt")
}