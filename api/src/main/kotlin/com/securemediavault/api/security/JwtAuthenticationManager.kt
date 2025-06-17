package com.securemediavault.api.security

import com.securemediavault.api.util.JWTUtils
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationManager(
    private val jwtUtils: JWTUtils
) {
    fun authenticate(token: String): Mono<Authentication> {
        return Mono.justOrEmpty(token)
            .filter { jwtUtils.validateToken(it) }
            .map {
                val username = jwtUtils.getUsernameFromToken(it)
                val roles = listOf(SimpleGrantedAuthority("ROLE_USER"))
                UsernamePasswordAuthenticationToken(username, null, roles)
            }
    }
}
