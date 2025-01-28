package app.keycloakdemobackend.model

data class Employee(
    val id: String?,
    val firstName: String,
    val lastName: String,
    val email: String,
    val contactNumber: String?,
    val roles: List<String> = emptyList(),
    val createdAt: Long?,
    val status: String,
)
