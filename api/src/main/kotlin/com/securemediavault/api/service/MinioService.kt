package com.securemediavault.api.service

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono
import java.io.InputStream

@Service
class MinioService(
    @Value("\${minio.url}") private val minioUrl: String,
    @Value("\${minio.accessKey}") private val accessKey: String,
    @Value("\${minio.secretKey}") private val secretKey: String
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
            fileName
        }
    }
}
