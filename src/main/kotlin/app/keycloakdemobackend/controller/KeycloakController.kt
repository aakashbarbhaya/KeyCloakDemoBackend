package app.keycloakdemobackend.controller

import app.keycloakdemobackend.model.TokenResponse
import app.keycloakdemobackend.service.KeycloakService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


data class LoginRequest(
    val username: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)


@CrossOrigin
@RestController
@RequestMapping("/api/v1")
class KeycloakController(
    private val keycloakService: KeycloakService
) {

    @PostMapping("/auth/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(keycloakService.login(request.username, request.password))
    }

    @PostMapping("/auth/refresh-token")
    fun refreshToken(@RequestBody refreshTokenRequest: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(keycloakService.refreshToken(refreshTokenRequest.refreshToken))
    }

    @GetMapping("/public")
    fun publicEndpoint() = "Public Endpoint"

    @GetMapping("/admin")
    fun adminEndpoint() = "Admin Endpoint"

    @GetMapping("/employee")
    fun employeeEndpoint() = "Employee Endpoint"
}