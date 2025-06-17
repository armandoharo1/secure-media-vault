package com.securemediavault.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.securemediavault.worker.dto.FileUploadedEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class FileUploadedConsumer(
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(FileUploadedConsumer::class.java)

    @RabbitListener(queues = ["file-uploaded-queue"])
    fun handleFileUploadedEvent(message: ByteArray) {
        try {
            val event = objectMapper.readValue(message, FileUploadedEvent::class.java)
            logger.info("📥 Archivo recibido: ${event.filename}, tamaño: ${event.size}, tipo: ${event.contentType}, subido: ${event.uploadedAt}")
        } catch (e: Exception) {
            logger.error("❌ Error al procesar mensaje: ${String(message)}", e)
        }
    }
}
