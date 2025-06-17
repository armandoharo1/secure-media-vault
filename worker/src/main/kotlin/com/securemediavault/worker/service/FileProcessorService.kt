package com.securemediavault.worker.service

import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths

@Service
class FileProcessorService {

    private val tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "secure-media-vault")

    init {
        Files.createDirectories(tempDir)
    }

    fun processFile(fileName: String, content: ByteArray) {
        val outputFile = tempDir.resolve(fileName).toFile()
        outputFile.writeBytes(content)

        println("Archivo procesado y guardado en: ${outputFile.absolutePath}")
    }
}
