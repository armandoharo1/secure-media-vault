package com.securemediavault.api.dto

import java.time.Instant

data class FileUploadedEvent(
    val filename: String,
    val size: Long,
    val contentType: String,
    val uploadedAt: Instant = Instant.now()
)
