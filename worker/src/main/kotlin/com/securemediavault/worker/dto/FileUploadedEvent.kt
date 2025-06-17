package com.securemediavault.worker.dto

import java.time.Instant

data class FileUploadedEvent @JvmOverloads constructor(
    val filename: String = "",
    val size: Long = 0,
    val contentType: String = "",
    val uploadedAt: Instant = Instant.now()
)
