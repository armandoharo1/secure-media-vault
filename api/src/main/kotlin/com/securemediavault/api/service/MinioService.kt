package com.securemediavault.api.service

import io.minio.*
import io.minio.messages.Item
import com.securemediavault.shared.dto.FileUploadedEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.InputStream
import java.time.Instant

@Service
class MinioService(
    @Value("\${minio.url}") private val minioUrl: String,
    @Value("\${minio.accessKey}") private val accessKey: String,
    @Value("\${minio.secretKey}") private val secretKey: String,
    private val rabbitTemplate: RabbitTemplate
) {

    private val client = MinioClient.builder()
        .endpoint(minioUrl)
        .credentials(accessKey, secretKey)
        .build()

    private val bucket = "media"

    init {
        ensureBucket()
    }

    private fun ensureBucket() {
        val exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }
    }

    fun upload(fileName: String, inputStream: InputStream, size: Long, contentType: String): Mono<String> {
        return Mono.fromCallable {
            client.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(fileName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build()
            )

            val event = FileUploadedEvent(
                filename = fileName,
                size = size,
                contentType = contentType,
                uploadedAt = Instant.now()
            )
            rabbitTemplate.convertAndSend("media.file.uploaded", "file-uploaded-queue", event)

            fileName
        }
    }

    fun listFiles(): Flux<String> {
        val results = client.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucket)
                .recursive(true)
                .build()
        )

        return Flux.fromIterable(results)
            .map { it.get().objectName() }
    }

    fun download(fileName: String): Mono<ByteArray> {
        return Mono.fromCallable {
            client.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(fileName)
                    .build()
            ).use { inputStream ->
                inputStream.readAllBytes()
            }
        }
    }

    fun deleteFile(fileName: String): Mono<Void> {
        return Mono.fromRunnable<Void> {
            client.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(fileName)
                    .build()
            )
            rabbitTemplate.convertAndSend("file_exchange", "file.deleted", fileName)
        }.then()
    }
}