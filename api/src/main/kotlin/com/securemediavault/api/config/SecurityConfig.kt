package com.securemediavault.api.config

import com.securemediavault.api.security.JwtSecurityContextRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtSecurityContextRepository: JwtSecurityContextRepository
) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .securityContextRepository(jwtSecurityContextRepository)
            .authorizeExchange {
                it.pathMatchers("/health", "/auth/**").permitAll()
                    .anyExchange().authenticated()
            }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .build()
    }
}
