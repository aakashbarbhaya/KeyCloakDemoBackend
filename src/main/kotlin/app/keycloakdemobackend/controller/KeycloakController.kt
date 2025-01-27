package app.keycloakdemobackend.controller

import app.keycloakdemobackend.service.KeycloakService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


data class LoginRequest(
    val username: String,
    val password: String
)

@CrossOrigin
@RestController
@RequestMapping("/")
class KeycloakController(
    private val keycloakService: KeycloakService
) {

    @PostMapping("/auth/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(keycloakService.authenticate(request.username, request.password))
    }

    @GetMapping("/public")
    fun publicEndpoint() = "Public Endpoint"

    @GetMapping("/admin")
    fun adminEndpoint() = "Admin Endpoint"

    @GetMapping("/employee")
    fun employeeEndpoint() = "Employee Endpoint"
}