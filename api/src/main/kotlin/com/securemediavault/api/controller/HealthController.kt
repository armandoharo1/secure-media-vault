package com.securemediavault.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@Tag(name = "Health Check", description = "Endpoint para verificar el estado de la aplicación")
class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Verifica el estado de salud de la aplicación", description = "Retorna el estado 'UP' si la aplicación está funcionando correctamente.")
    fun healthCheck(): Mono<Map<String, String>> {
        return Mono.just(mapOf("status" to "UP"))
    }
}