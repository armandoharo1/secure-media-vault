package com.securemediavault.api.controller

import com.securemediavault.api.dto.LoginRequest
import com.securemediavault.api.dto.LoginResponse
import com.securemediavault.api.dto.RegisterRequest
import com.securemediavault.api.model.User
import com.securemediavault.api.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import com.securemediavault.api.dto.RefreshRequest


@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación de Usuarios", description = "Endpoints para el registro, inicio de sesión y gestión de tokens de usuario")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Operation(summary = "Registra un nuevo usuario en el sistema", description = "Crea una nueva cuenta de usuario con nombre de usuario y contraseña.")
    fun register(@Valid @RequestBody request: RegisterRequest): Mono<User> {
        return authService.register(request)
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y obtiene tokens JWT", description = "Autentica al usuario y devuelve un token de acceso y un token de refresco.")
    fun login(@Valid @RequestBody request: LoginRequest): Mono<LoginResponse> {
        return authService.login(request.username, request.password)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresca el token de acceso usando un token de refresco")
    fun refresh(@RequestBody request: RefreshRequest): Mono<LoginResponse> {
        return authService.refreshAccessToken(request.refreshToken)
    }

}