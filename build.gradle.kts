val kotlin_version: String by project
val logback_version: String by project

plugins {
	kotlin("jvm") version "2.1.10"
	id("io.ktor.plugin") version "3.1.2"
	id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

group = "dev.ansgrb"
version = "0.0.1"

application {
	mainClass = "io.ktor.server.netty.EngineMain"

	val isDevelopment: Boolean = project.ext.has("development")
	applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("io.ktor:ktor-server-core")
	implementation("io.ktor:ktor-server-websockets")
	implementation("io.ktor:ktor-server-content-negotiation")
	implementation("io.ktor:ktor-serialization-kotlinx-json")
	implementation("io.ktor:ktor-server-netty")
	implementation("ch.qos.logback:logback-classic:$logback_version")
	implementation("io.ktor:ktor-server-config-yaml")
	implementation("io.ktor:ktor-server-cors")
	implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.1.3")
	implementation("io.ktor:ktor-server-auth:2.3.7")
	implementation("io.ktor:ktor-server-auth-jwt:2.3.7")
	implementation("com.auth0:jwks-rsa:0.22.1")
	implementation("org.slf4j:slf4j-simple:2.0.13")
	implementation("io.ktor:ktor-server-status-pages")
	testImplementation("io.ktor:ktor-server-test-host")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
