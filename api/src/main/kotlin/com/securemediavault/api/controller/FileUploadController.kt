package com.securemediavault.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/files")
class FileUploadController {

    @PostMapping("/upload")
    fun upload(): Mono<ResponseEntity<String>> {
        return Mono.just(ResponseEntity.ok("Subida protegida exitosa"))
    }
}
