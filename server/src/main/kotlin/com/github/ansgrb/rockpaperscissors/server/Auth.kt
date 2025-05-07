package com.github.ansgrb.rockpaperscissors.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.Date

data class User(
	val id: String,
	val name: String,
)

// shared function to get config values to ensure consistency
private fun getConfigValue(application: Application, key: String, default: String): String {
	return application.environment.config.propertyOrNull(key)?.getString() ?: default
}

fun Application.configureAuth() {

	// JWT configuration from application.conf file
	val secret = getConfigValue(this, "jwt.secret", "secret")
	val issuer = getConfigValue(this, "jwt.issuer", "http://localhost:8080")
	val audience = getConfigValue(this, "jwt.audience", "http://localhost:8080")


	// JWT Authentication Provider
	install(Authentication) {
		jwt("auth-jwt") {
			realm = "RPS API"
			// Configure the JWT verifier with the secret, audience, and issuer
			verifier(
				JWT.require(Algorithm.HMAC256(secret))
					.withAudience(audience)
					.withIssuer(issuer)
					.build()
			)
			// Validate the JWT token and extract the principal
			validate { credential ->
				val id = credential.payload.getClaim("id").asString()
				if (id != null && id.isNotEmpty()) {
					JWTPrincipal(credential.payload)
				} else {
					null // Invalid token
				}
			}
		}
	}
}

fun Application.generateToken(user: User): String {
	// read config using the shared function
	val secret = getConfigValue(this, "jwt.secret", "secret")
	val issuer = getConfigValue(this, "jwt.issuer", "http://localhost:8080")
	val audience = getConfigValue(this, "jwt.audience", "http://localhost:8080")

	return JWT.create()
		.withAudience(audience)
		.withIssuer(issuer)
		.withClaim("id", user.id)
		.withClaim("name", user.name)
		.withExpiresAt(Date(System.currentTimeMillis() + 604800000)) // 7 days in milliseconds duh!
		.sign(Algorithm.HMAC256(secret))
}
