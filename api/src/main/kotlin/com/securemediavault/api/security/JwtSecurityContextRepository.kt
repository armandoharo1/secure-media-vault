package com.securemediavault.api.security

import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtSecurityContextRepository(
    private val jwtAuthManager: JwtAuthenticationManager
) : ServerSecurityContextRepository {

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val authToken = authHeader.substring(7)
            jwtAuthManager.authenticate(authToken)
                .map { SecurityContextImpl(it) }
        } else {
            Mono.empty()
        }
    }

    override fun save(exchange: ServerWebExchange, context: SecurityContext): Mono<Void> {
        return Mono.empty() // No necesitas guardar contexto para JWT stateless
    }
}
