package com.securemediavault.worker
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import com.securemediavault.shared.dto.FileUploadedEvent
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
