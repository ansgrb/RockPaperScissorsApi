plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")
	id("io.ktor.plugin")
}

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

application {
	mainClass.set("com.github.ansgrb.rockpaperscissorsapi.server.ApplicationKt")
}

tasks.test {
	systemProperty("test", "false")
	useJUnitPlatform()
}



dependencies {
	implementation(project(":shared"))

	implementation("io.ktor:ktor-server-core:${ktorVersion}")
	implementation("io.ktor:ktor-server-websockets:${ktorVersion}")
	implementation("io.ktor:ktor-server-content-negotiation:${ktorVersion}")
	implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
	implementation("io.ktor:ktor-server-netty:${ktorVersion}")
	implementation("io.ktor:ktor-server-config-yaml:${ktorVersion}")
	implementation("io.ktor:ktor-server-cors:${ktorVersion}")
	implementation("io.ktor:ktor-server-auth:${ktorVersion}")
	implementation("io.ktor:ktor-server-auth-jwt:${ktorVersion}")
	implementation("io.ktor:ktor-server-status-pages:${ktorVersion}")
	implementation("io.ktor:ktor-server-rate-limit:${ktorVersion}")

	implementation("ch.qos.logback:logback-classic:$logbackVersion")
	implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.1.3")
	implementation("com.auth0:jwks-rsa:0.22.1")
//	implementation("org.slf4j:slf4j-simple:2.0.13")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0") // Use a recent version
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0") // Use the same version as api
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${kotlinVersion}")


//	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${kotlin_version}")
//	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
//	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
//	testImplementation("io.mockk:mockk:1.13.8")

}
