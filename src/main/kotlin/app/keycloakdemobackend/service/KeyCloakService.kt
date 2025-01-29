package app.keycloakdemobackend.service

import app.keycloakdemobackend.model.KeycloakRole
import app.keycloakdemobackend.model.KeycloakUser
import app.keycloakdemobackend.model.TokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class KeycloakService(
    private val webClient: WebClient.Builder,
) {
    @Value("\${keycloak.auth-server-url}")
    lateinit var authServerUrl: String

    @Value("\${keycloak.realm}")
    lateinit var realm: String

    @Value("\${keycloak.clientId}")
    lateinit var clientId: String

    @Value("\${keycloak.client_key}")
    lateinit var clientKey: String

    @Value("\${keycloak.credentials.secret}")
    lateinit var clientSecret: String

    fun login(username: String, password: String): TokenResponse {
        val tokenEndpoint = "$authServerUrl/realms/$realm/protocol/openid-connect/token"

        return webClient.build()
            .post()
            .uri(tokenEndpoint)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue(
                "grant_type=password" +
                        "&client_id=$clientId" +
                        "&client_secret=$clientSecret" +
                        "&username=$username" +
                        "&password=$password"
            )
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .block() ?: throw RuntimeException("Failed to authenticate with Keycloak")
    }

    fun createUser(
        firstName: String,
        lastName: String? = null,
        email: String,
        password: String,
        roles: List<String>
    ): String {
        val token = fetchAccessToken()
        val userEndpoint = "$authServerUrl/admin/realms/$realm/users"

        // Prepare user creation payload
        val userPayload = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "username" to email,
            "enabled" to true,
            "credentials" to listOf(
                mapOf(
                    "type" to "password",
                    "value" to password,
                    "temporary" to false
                )
            )
        )

        // Create user in Keycloak
        webClient.build()
            .post()
            .uri(userEndpoint)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(userPayload)
            .retrieve()
            .onStatus({ it.isError }) {
                Mono.error(RuntimeException("Failed to create user: ${it.statusCode()}"))
            }
            .bodyToMono(Void::class.java)
            .block()

        val userId = getUserIdByUsername(email, token)
        assignRolesToUser(userId, roles, token)

        return userId
    }

    private fun getUserIdByUsername(username: String, token: String): String {
        val searchEndpoint = "$authServerUrl/admin/realms/$realm/users?username=$username"

        val users = webClient.build()
            .get()
            .uri(searchEndpoint)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .bodyToMono(Array<KeycloakUser>::class.java)
            .block() ?: throw RuntimeException("Failed to retrieve user by username")

        return users.firstOrNull()?.id ?: throw RuntimeException("User not found")
    }

    private fun assignRolesToUser(userId: String, roles: List<String>, token: String) {
        val rolesEndpoint = "$authServerUrl/admin/realms/$realm/users/$userId/role-mappings/clients/$clientKey"

        val roleMappings = roles.map { roleName ->
            getRoleDetails(roleName, clientKey, token)
        }

        webClient.build()
            .post()
            .uri(rolesEndpoint)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .bodyValue(roleMappings)
            .retrieve()
            .onStatus({ it.isError }) {
                Mono.error(RuntimeException("Failed to assign roles: ${it.statusCode()}"))
            }
            .bodyToMono(Void::class.java)
            .block()
    }

    private fun getRoleDetails(roleName: String, clientKey: String, token: String): KeycloakRole {
        val rolesEndpoint = "$authServerUrl/admin/realms/$realm/clients/$clientKey/roles/$roleName"

        return webClient.build()
            .get()
            .uri(rolesEndpoint)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .bodyToMono(KeycloakRole::class.java)
            .block() ?: throw RuntimeException("Role not found: $roleName")
    }


    private fun fetchAccessToken(): String {
        val tokenEndpoint = "$authServerUrl/realms/$realm/protocol/openid-connect/token"

        val response = webClient.build()
            .post()
            .uri(tokenEndpoint)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue(
                "grant_type=client_credentials" +
                        "&client_id=$clientId" +
                        "&client_secret=$clientSecret"
            )
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .block() ?: throw RuntimeException("Failed to fetch access token")

        return response.access_token
    }
}