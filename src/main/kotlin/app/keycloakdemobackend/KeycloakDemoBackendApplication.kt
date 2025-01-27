package app.keycloakdemobackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KeycloakDemoBackendApplication

fun main(args: Array<String>) {
    runApplication<KeycloakDemoBackendApplication>(*args)
}
