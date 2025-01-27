package app.keycloakdemobackend.config

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import java.util.stream.Collectors

class KeycloakRoleConverter : org.springframework.core.convert.converter.Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val roles = jwt.claims["realm_access"]?.let { (it as Map<*, *>)["roles"] as Collection<*> }
        return roles?.map { SimpleGrantedAuthority("ROLE_$it") } ?: emptyList()
    }
}