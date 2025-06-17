package com.securemediavault.worker

import com.securemediavault.shared.dto.FileUploadedEvent
import com.securemediavault.worker.service.FileProcessorService
import io.minio.GetObjectArgs
import io.minio.MinioClient
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FileUploadedListener(
    private val fileProcessorService: FileProcessorService,
    @Value("\${minio.url}") private val minioUrl: String,
    @Value("\${minio.accessKey}") private val accessKey: String,
    @Value("\${minio.secretKey}") private val secretKey: String
) {
    private val bucket = "media"

    private val minioClient: MinioClient = MinioClient.builder()
        .endpoint(minioUrl)
        .credentials(accessKey, secretKey)
        .build()

    @RabbitListener(queues = ["file-uploaded-queue"])
    fun handleFileUploadedEvent(event: FileUploadedEvent) {
        val inputStream = minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucket)
                .`object`(event.filename)
                .build()
        )

        val content = inputStream.use { it.readAllBytes() }

        println("Archivo recibido en WORKER:")
        println("Nombre: ${event.filename}")
        println("Tamaño: ${event.size} bytes")
        println("Tipo: ${event.contentType}")
        println("Fecha: ${event.uploadedAt}")

        fileProcessorService.processFile(event.filename, content)
    }
}
