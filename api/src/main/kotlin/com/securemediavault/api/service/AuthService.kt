package com.securemediavault.api.service

import com.securemediavault.api.dto.LoginResponse
import com.securemediavault.api.dto.RegisterRequest
import com.securemediavault.api.model.User
import com.securemediavault.api.repository.UserRepository
import com.securemediavault.api.util.JWTUtils
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtUtils: JWTUtils // ✅ Inyección de dependencia
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    fun register(request: RegisterRequest): Mono<User> {
        val encodedPassword = passwordEncoder.encode(request.password)
        val user = User(
            username = request.username,
            password = encodedPassword,
            roles = "ROLE_USER"
        )
        return userRepository.save(user)
    }

    fun login(username: String, rawPassword: String): Mono<LoginResponse> {
        println("🔑 Login request for $username with password: $rawPassword")
        return userRepository.findByUsername(username)
            .doOnNext { println("🎯 Found user: ${it.username}, encoded: ${it.password}") }
            .filter { passwordEncoder.matches(rawPassword, it.password) }
            .switchIfEmpty(Mono.error(Exception("Invalid credentials")))
            .map {
                val accessToken = jwtUtils.generateAccessToken(it.username)
                val refreshToken = jwtUtils.generateRefreshToken(it.username)
                LoginResponse(accessToken, refreshToken)
            }
    }


    fun refreshAccessToken(refreshToken: String): Mono<LoginResponse> {
        return Mono.fromCallable {
            if (!jwtUtils.validateToken(refreshToken)) {
                throw Exception("Invalid refresh token")
            }
            val username = jwtUtils.getUsernameFromToken(refreshToken)
            val newAccessToken = jwtUtils.generateAccessToken(username)
            val newRefreshToken = jwtUtils.generateRefreshToken(username)
            LoginResponse(newAccessToken, newRefreshToken)
        }
    }

}
