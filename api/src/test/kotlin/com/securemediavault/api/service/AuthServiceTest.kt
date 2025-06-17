package com.securemediavault.api.service


import com.securemediavault.api.dto.RegisterRequest
import com.securemediavault.api.model.User
import com.securemediavault.api.repository.UserRepository
import com.securemediavault.api.util.JWTUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var jwtUtils: JWTUtils
    private lateinit var authService: AuthService

    private val passwordEncoder = BCryptPasswordEncoder()

    @BeforeEach
    fun setUp() {
        // Inicializa los mocks para cada test
        userRepository = mockk()
        jwtUtils = mockk()
        authService = AuthService(userRepository, jwtUtils)
    }

    @Test
    fun `register should encode password and save user`() {
        val request = RegisterRequest("testuser", "password123")
        val savedUser = User(id = UUID.randomUUID(), username = "testuser", password = passwordEncoder.encode("password123"), roles = "ROLE_USER")

        every { userRepository.save(any<User>()) } returns Mono.just(savedUser)

        StepVerifier.create(authService.register(request))
            .expectNextMatches {
                it.username == "testuser" && passwordEncoder.matches("password123", it.password)
            }
            .verifyComplete()
        verify(exactly = 1) { userRepository.save(any<User>()) }
    }

    @Test
    fun `login should return tokens for valid credentials`() {
        val username = "testuser"
        val rawPassword = "password123"
        val encodedPassword = passwordEncoder.encode(rawPassword)
        val user = User(id = UUID.randomUUID(), username = username, password = encodedPassword, roles = "ROLE_USER")
        val accessToken = "mockedAccessToken"
        val refreshToken = "mockedRefreshToken"

        every { userRepository.findByUsername(username) } returns Mono.just(user)
        every { jwtUtils.generateAccessToken(username) } returns accessToken
        every { jwtUtils.generateRefreshToken(username) } returns refreshToken

        StepVerifier.create(authService.login(username, rawPassword))
            .expectNextMatches { it.accessToken == accessToken && it.refreshToken == refreshToken }
            .verifyComplete()

        verify(exactly = 1) { userRepository.findByUsername(username) }
        verify(exactly = 1) { jwtUtils.generateAccessToken(username) }
        verify(exactly = 1) { jwtUtils.generateRefreshToken(username) }
    }

    @Test
    fun `login should return error for invalid password`() {
        val username = "testuser"
        val rawPassword = "wrongpassword"
        val encodedPassword = passwordEncoder.encode("password123")
        val user = User(id = UUID.randomUUID(), username = username, password = encodedPassword, roles = "ROLE_USER")

        every { userRepository.findByUsername(username) } returns Mono.just(user)

        StepVerifier.create(authService.login(username, rawPassword))
            .expectErrorMatches { it.message == "Invalid credentials" }
            .verify()

        verify(exactly = 1) { userRepository.findByUsername(username) }
        verify(exactly = 0) { jwtUtils.generateAccessToken(any()) }
        verify(exactly = 0) { jwtUtils.generateRefreshToken(any()) }
    }

    @Test
    fun `login should return error for user not found`() {
        val username = "nonexistentuser"
        val rawPassword = "password123"

        every { userRepository.findByUsername(username) } returns Mono.empty()

        StepVerifier.create(authService.login(username, rawPassword))
            .expectErrorMatches { it.message == "Invalid credentials" }
            .verify()

        verify(exactly = 1) { userRepository.findByUsername(username) }
        verify(exactly = 0) { jwtUtils.generateAccessToken(any()) }
        verify(exactly = 0) { jwtUtils.generateRefreshToken(any()) }
    }

    @Test
    fun `refreshAccessToken should return new tokens for valid refresh token`() {
        val refreshToken = "validRefreshToken"
        val username = "testuser"
        val newAccessToken = "newAccessToken"
        val newRefreshToken = "newRefreshToken"

        every { jwtUtils.validateToken(refreshToken) } returns true
        every { jwtUtils.getUsernameFromToken(refreshToken) } returns username
        every { jwtUtils.generateAccessToken(username) } returns newAccessToken
        every { jwtUtils.generateRefreshToken(username) } returns newRefreshToken

        StepVerifier.create(authService.refreshAccessToken(refreshToken))
            .expectNextMatches { it.accessToken == newAccessToken && it.refreshToken == newRefreshToken }
            .verifyComplete()

        verify(exactly = 1) { jwtUtils.validateToken(refreshToken) }
        verify(exactly = 1) { jwtUtils.getUsernameFromToken(refreshToken) }
        verify(exactly = 1) { jwtUtils.generateAccessToken(username) }
        verify(exactly = 1) { jwtUtils.generateRefreshToken(username) }
    }

    @Test
    fun `refreshAccessToken should return error for invalid refresh token`() {
        val refreshToken = "invalidRefreshToken"

        every { jwtUtils.validateToken(refreshToken) } returns false

        StepVerifier.create(authService.refreshAccessToken(refreshToken))
            .expectErrorMatches { it.message == "Invalid refresh token" }
            .verify()

        verify(exactly = 1) { jwtUtils.validateToken(refreshToken) }
        verify(exactly = 0) { jwtUtils.getUsernameFromToken(any()) }
        verify(exactly = 0) { jwtUtils.generateAccessToken(any()) }
        verify(exactly = 0) { jwtUtils.generateRefreshToken(any()) }
    }
}