package app.keycloakdemobackend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

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

        return response as? Map<String, Any> ?: throw RuntimeException("Failed to authenticate with Keycloak")
    }

    fun createUser(
        firstName: String,
        lastName: String? = null,
        email: String,
        password: String,
        roles: List<String>
    ): String {
        try {
            val token = fetchAccessToken()
            val userEndpoint = "$authServerUrl/admin/realms/$realm/users"

            // Create user payload
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

            // Create user
            val response = webClient.build()
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

            // Get the created user's ID
            val userId = getUserIdByUsername(email, token)

            // Assign roles to the user
            assignRolesToUser(userId, roles, token)

            return "User created successfully with ID: $userId"
        } catch (e: Exception) {
            throw RuntimeException("Failed to create user", e)
        }
    }

    private fun getUserIdByUsername(username: String, token: String): String {
        val searchEndpoint = "$authServerUrl/admin/realms/$realm/users?username=$username"

        val users = webClient.build()
            .get()
            .uri(searchEndpoint)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .bodyToMono(List::class.java)
            .block() ?: throw RuntimeException("Failed to retrieve user by username")

        val user = users.firstOrNull() as? Map<String, Any>
            ?: throw RuntimeException("User not found")

        return user["id"] as? String ?: throw RuntimeException("User ID not found")
    }

    private fun assignRolesToUser(userId: String, roles: List<String>, token: String) {
        // Fetch the internal client ID
        val clientId = getClientId(token)

        // Endpoint to assign roles
        val rolesEndpoint = "$authServerUrl/admin/realms/$realm/users/$userId/role-mappings/clients/$clientId"

        // Fetch role details and prepare payload
        val roleMappings = roles.map { roleName ->
            val role = getRoleDetails(roleName, clientId, token)
            mapOf(
                "id" to role["id"],
                "name" to role["name"]
            )
        }

        // Assign roles to the user
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

    private fun getRoleDetails(roleName: String, clientId: String, token: String): Map<String, Any> {
        val rolesEndpoint = "$authServerUrl/admin/realms/$realm/clients/$clientId/roles/$roleName"

        val role = webClient.build()
            .get()
            .uri(rolesEndpoint)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() ?: throw RuntimeException("Role not found: $roleName")

        return role as Map<String, Any> ?: throw RuntimeException("Role not found")
    }

    private fun getClientId(token: String): String {
        val clientsEndpoint = "$authServerUrl/admin/realms/$realm/clients"

        val clients = webClient.build()
            .get()
            .uri(clientsEndpoint)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .bodyToMono(List::class.java)
            .block() ?: throw RuntimeException("Failed to retrieve clients")

        val client = clients.firstOrNull { (it as Map<String, Any>)["clientId"] == clientId } as? Map<String, Any>
            ?: throw RuntimeException("Client not found")

        return client["id"] as? String ?: throw RuntimeException("Client ID not found")
    }

    private fun fetchAccessToken(): String {
        try {
            val tokenEndpoint = "$authServerUrl/realms/$realm/protocol/openid-connect/token"

            val response = webClient.build()
                .post()
                .uri(tokenEndpoint)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(
                    "grant_type=client_credentials&client_id=$clientId&client_secret=$clientSecret"
                )
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            return response?.get("access_token")?.toString()
                ?: throw RuntimeException("Failed to fetch access token")
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch access token", e)
        }

    }
}