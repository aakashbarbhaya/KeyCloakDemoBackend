package app.keycloakdemobackend.model

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String? = null
)

data class KeycloakUser(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String?,
    val email: String,
    val enabled: Boolean
)

data class KeycloakRole(
    val id: String,
    val name: String
)