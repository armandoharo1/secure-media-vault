package com.securemediavault.worker.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.stream.Collectors

@RestController
class FileQueryController {

    private val baseDir: Path = Paths.get(System.getProperty("java.io.tmpdir"), "secure-media-vault")

    data class FileInfo(
        val filename: String,
        val contentType: String,
        val size: Long,
        val uploadedAt: Instant,
        val path: String
    )

    @GetMapping("/files")
    fun listFiles(): List<FileInfo> {
        if (!Files.exists(baseDir)) return emptyList()

        return Files.list(baseDir)
            .filter { Files.isRegularFile(it) }
            .map {
                FileInfo(
                    filename = it.fileName.toString(),
                    contentType = Files.probeContentType(it) ?: "application/octet-stream",
                    size = Files.size(it),
                    uploadedAt = Files.getLastModifiedTime(it).toInstant(),
                    path = it.toAbsolutePath().toString()
                )
            }
            .collect(Collectors.toList())
    }
}
