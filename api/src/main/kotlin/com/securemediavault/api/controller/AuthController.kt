package com.securemediavault.api.controller

import com.securemediavault.api.dto.LoginRequest
import com.securemediavault.api.dto.LoginResponse
import com.securemediavault.api.dto.RegisterRequest
import com.securemediavault.api.model.User
import com.securemediavault.api.service.AuthService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import com.securemediavault.api.dto.RefreshRequest


@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): Mono<User> {
        return authService.register(request)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Mono<LoginResponse> {
        return authService.login(request.username, request.password)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): Mono<LoginResponse> {
        return authService.refreshAccessToken(request.refreshToken)
    }

}
