plugins {
	kotlin("jvm")
	application
}

dependencies {
	implementation(project(":shared"))
	implementation("com.github.ajalt.clikt:clikt:4.2.1")
}

application {
	mainClass.set("dev.ansgrb.cli.MainKt")
}