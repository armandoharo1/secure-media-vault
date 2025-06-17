package com.securemediavault.api.controller

import com.securemediavault.api.service.MinioService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/files")
@Tag(name = "Operaciones de Archivos", description = "Endpoints para subir, listar, descargar y eliminar archivos en MinIO")
class FileUploadController(
    private val minioService: MinioService
) {

    @PostMapping(
        "/upload",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @Operation(summary = "Sube un nuevo archivo a MinIO", description = "Permite subir archivos de medios (imágenes, videos, documentos, audio) al almacenamiento de objetos.")
    fun uploadFile(@RequestPart("file") file: FilePart): Mono<ResponseEntity<String>> {
        return DataBufferUtils.join(file.content())
            .flatMap { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                DataBufferUtils.release(dataBuffer)
                val inputStream = bytes.inputStream()
                minioService.upload(
                    file.filename(),
                    inputStream,
                    bytes.size.toLong(),
                    file.headers().contentType?.toString() ?: "application/octet-stream"
                )
            }
            .map { ResponseEntity.ok("Archivo '$it' subido correctamente a MinIO") }
            .onErrorResume { e ->
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el archivo: ${e.message}"))
            }
    }

    @GetMapping("/list")
    @Operation(summary = "Lista todos los archivos almacenados en MinIO", description = "Recupera una lista de nombres de todos los archivos disponibles en el vault.")
    fun listFiles(): Flux<String> {
        return minioService.listFiles()
            .onErrorResume { e ->
                Flux.error(org.springframework.web.server.ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al listar archivos: ${e.message}", e))
            }
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "Descarga un archivo específico de MinIO", description = "Permite descargar un archivo por su nombre, con soporte para streaming.")
    fun downloadFile(@PathVariable fileName: String): Mono<ResponseEntity<ByteArray>> {
        return minioService.download(fileName)
            .map { bytes ->
                ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"$fileName\"")
                    .contentLength(bytes.size.toLong())
                    .body(bytes)
            }
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume { e ->
                if (e.message?.contains("NoSuchKey") == true) {
                    Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Archivo '$fileName' no encontrado.".toByteArray()))
                } else {
                    Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al descargar el archivo '$fileName': ${e.message}".toByteArray()))
                }
            }
    }

    @DeleteMapping("/delete/{fileName}")
    @Operation(summary = "Elimina un archivo específico de MinIO", description = "Realiza una eliminación permanente (hard delete) de un archivo del almacenamiento de objetos.")
    fun deleteFile(@PathVariable fileName: String): Mono<ResponseEntity<String>> {
        return minioService.deleteFile(fileName)
            .thenReturn(ResponseEntity.ok("Archivo '$fileName' eliminado correctamente de MinIO"))
            .onErrorResume { e ->
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el archivo '$fileName': ${e.message}"))
            }
    }
}