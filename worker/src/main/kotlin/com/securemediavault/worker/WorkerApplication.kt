package com.securemediavault.worker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WorkerApplication

fun main(args: Array<String>) {
	runApplication<WorkerApplication>(*args)
}
