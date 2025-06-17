package com.securemediavault.api.controller

import com.securemediavault.api.service.MinioService
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/files")
class FileUploadController(
    private val minioService: MinioService
) {

    @PostMapping(
        "/upload",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadFile(@RequestPart("file") file: FilePart): Mono<ResponseEntity<String>> {
        return DataBufferUtils.join(file.content())
            .flatMap { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                DataBufferUtils.release(dataBuffer) // Liberar recursos
                val inputStream = bytes.inputStream()
                minioService.upload(
                    file.filename(),
                    inputStream,
                    bytes.size.toLong(),
                    file.headers().contentType?.toString() ?: "application/octet-stream"
                )
            }
            .map { ResponseEntity.ok("Archivo '$it' subido correctamente a MinIO") }
    }
}
