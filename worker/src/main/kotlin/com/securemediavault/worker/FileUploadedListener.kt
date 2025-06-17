package com.securemediavault.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.securemediavault.worker.dto.FileUploadedEvent
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class FileUploadedListener(
    private val objectMapper: ObjectMapper
) {

    @RabbitListener(queues = ["file-uploaded-queue"])
    fun handleFileUploadedEvent(message: String) {
        val event = objectMapper.readValue(message, FileUploadedEvent::class.java)
        println("📥 Archivo recibido en WORKER:")
        println("📁 Nombre: ${event.filename}")
        println("📦 Tamaño: ${event.size} bytes")
        println("🧾 Tipo: ${event.contentType}")
        println("⏱️ Fecha: ${event.uploadedAt}")
    }
}
