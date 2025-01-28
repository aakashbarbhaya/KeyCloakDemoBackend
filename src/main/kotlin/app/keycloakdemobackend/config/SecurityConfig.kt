package app.keycloakdemobackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    private val keycloakIssuerUrl = "http://localhost:8080/realms/KeyCloakDemoBackend"
    private val clientId = "keycloak-demo-spring-boot"

    @Bean
    fun jwtDecoder(): JwtDecoder = JwtDecoders.fromIssuerLocation(keycloakIssuerUrl)

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/public/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("admin")
                    .requestMatchers("/employee/**").hasRole("employee")
                    .requestMatchers("/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt -> extractAuthorities(jwt) }
        return jwtAuthenticationConverter
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        val resourceAccess = jwt.claims["resource_access"] as? Map<String, Any>
        val clientRoles = resourceAccess?.get(clientId) as? Map<String, Any>
        val roles = clientRoles?.get("roles") as? List<String>

        roles?.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        return authorities
    }
}