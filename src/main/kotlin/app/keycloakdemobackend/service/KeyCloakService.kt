package app.keycloakdemobackend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class KeycloakService(
    private val webClient: WebClient.Builder
) {
    @Value("\${keycloak.auth-server-url}")
    lateinit var authServerUrl: String

    @Value("\${keycloak.realm}")
    lateinit var realm: String

    @Value("\${keycloak.resource}")
    lateinit var clientId: String

    @Value("\${keycloak.credentials.secret}")
    lateinit var clientSecret: String

    fun login(username: String, password: String): Map<String, Any> {
        val tokenEndpoint = "$authServerUrl/realms/$realm/protocol/openid-connect/token"

        val response = webClient.build()
            .post()
            .uri(tokenEndpoint)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue(
                "grant_type=password&client_id=$clientId&client_secret=$clientSecret&username=$username&password=$password"
            )
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()

        // Cast the result to the expected type
        return response as? Map<String, Any> ?: throw RuntimeException("Failed to authenticate with Keycloak")
    }
}