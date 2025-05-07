plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")
	id("io.ktor.plugin")
}

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

application {
	mainClass.set("dev.ansgrb.ApplicationKt")
}

tasks.test {
	systemProperty("test", "false")
	useJUnitPlatform()
}



dependencies {
	implementation(project(":shared"))

	implementation("io.ktor:ktor-server-core:${ktor_version}")
	implementation("io.ktor:ktor-server-websockets:${ktor_version}")
	implementation("io.ktor:ktor-server-content-negotiation:${ktor_version}")
	implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")
	implementation("io.ktor:ktor-server-netty:${ktor_version}")
	implementation("io.ktor:ktor-server-config-yaml:${ktor_version}")
	implementation("io.ktor:ktor-server-cors:${ktor_version}")
	implementation("io.ktor:ktor-server-auth:${ktor_version}")
	implementation("io.ktor:ktor-server-auth-jwt:${ktor_version}")
	implementation("io.ktor:ktor-server-status-pages:${ktor_version}")
	implementation("io.ktor:ktor-server-rate-limit:${ktor_version}")

	implementation("ch.qos.logback:logback-classic:$logback_version")
	implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.1.3")
	implementation("com.auth0:jwks-rsa:0.22.1")
	implementation("org.slf4j:slf4j-simple:2.0.13")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0") // Use a recent version
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0") // Use the same version as api
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${kotlin_version}")


//	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${kotlin_version}")
//	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
//	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
//	testImplementation("io.mockk:mockk:1.13.8")

}
