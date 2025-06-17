package com.securemediavault.api.service

import com.securemediavault.shared.dto.FileUploadedEvent
import io.minio.*
import io.minio.messages.Item
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import reactor.test.StepVerifier
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.Instant

class MinioServiceTest {

    private lateinit var minioClient: MinioClient
    private lateinit var rabbitTemplate: RabbitTemplate
    private lateinit var minioService: MinioService

    private val minioUrl = "http://localhost:9000"
    private val accessKey = "minioaccesskey"
    private val secretKey = "miniosecretkey"
    private val bucketName = "media"

    @BeforeEach
    fun setUp() {
        minioClient = mockk(relaxed = true)
        rabbitTemplate = mockk(relaxed = true)

        mockkStatic(MinioClient::class)
        every {
            MinioClient.builder()
                .endpoint(any<String>())
                .credentials(any(), any())
                .build()
        } returns minioClient

        every { minioClient.bucketExists(any<BucketExistsArgs>()) } returns false
        every { minioClient.makeBucket(any<MakeBucketArgs>()) } returns Unit

        minioService = MinioService(minioUrl, accessKey, secretKey, rabbitTemplate)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(MinioClient::class)
    }

    @Test
    fun `ensureBucket should create bucket if it does not exist`() {
        verify(exactly = 1) { minioClient.bucketExists(any<BucketExistsArgs>()) }
        verify(exactly = 1) { minioClient.makeBucket(any<MakeBucketArgs>()) }
    }

    @Test
    fun `ensureBucket should not create bucket if it already exists`() {
        unmockkStatic(MinioClient::class)
        mockkStatic(MinioClient::class)
        every {
            MinioClient.builder()
                .endpoint(any<String>())
                .credentials(any(), any())
                .build()
        } returns minioClient

        every { minioClient.bucketExists(any<BucketExistsArgs>()) } returns true
        minioService = MinioService(minioUrl, accessKey, secretKey, rabbitTemplate)

        verify(exactly = 1) { minioClient.bucketExists(any<BucketExistsArgs>()) }
        verify(exactly = 0) { minioClient.makeBucket(any<MakeBucketArgs>()) }
    }

    @Test
    fun `upload should put object and send rabbitmq event`() {
        val fileName = "test.txt"
        val content = "hello world".toByteArray()
        val inputStream: InputStream = ByteArrayInputStream(content)
        val size = content.size.toLong()
        val contentType = "text/plain"

        val mockObjectWriteResponse = mockk<ObjectWriteResponse>(relaxed = true)
        every { minioClient.putObject(any<PutObjectArgs>()) } returns mockObjectWriteResponse

        StepVerifier.create(minioService.upload(fileName, inputStream, size, contentType))
            .expectNext(fileName)
            .verifyComplete()

        verify(exactly = 1) {
            minioClient.putObject(match<PutObjectArgs> {
                it.bucket() == bucketName &&
                        it.`object`() == fileName &&
                        it.contentType() == contentType
            })
        }
        verify(exactly = 1) {
            rabbitTemplate.convertAndSend(
                eq("media.file.uploaded"),
                eq("file-uploaded-queue"),
                match<FileUploadedEvent> {
                    it.filename == fileName &&
                            it.size == size && // Esta es la verificación clave del tamaño
                            it.contentType == contentType &&
                            it.uploadedAt.isBefore(Instant.now().plusSeconds(1))
                }
            )
        }
    }

    @Test
    fun `listFiles should return Flux of object names`() {
        val item1 = mockk<Item>()
        every { item1.objectName() } returns "file1.txt"
        val item2 = mockk<Item>()
        every { item2.objectName() } returns "file2.txt"

        val resultIterable: Iterable<Result<Item>> = listOf(
            Result(item1),
            Result(item2)
        )

        every { minioClient.listObjects(any<ListObjectsArgs>()) } returns resultIterable

        StepVerifier.create(minioService.listFiles())
            .expectNext("file1.txt")
            .expectNext("file2.txt")
            .verifyComplete()

        verify(exactly = 1) { minioClient.listObjects(any<ListObjectsArgs>()) }
    }

    @Test
    fun `download should return Mono of ByteArray`() {
        val fileName = "test.txt"
        val content = "downloaded content".toByteArray()
        val mockInputStream = ByteArrayInputStream(content)

        every { minioClient.getObject(any<GetObjectArgs>()) } returns GetObjectResponse(null, null, null, null, mockInputStream)

        StepVerifier.create(minioService.download(fileName))
            .expectNext(content)
            .verifyComplete()

        verify(exactly = 1) {
            minioClient.getObject(match<GetObjectArgs> {
                it.bucket() == bucketName && it.`object`() == fileName
            })
        }
    }

    @Test
    fun `deleteFile should remove object and send rabbitmq event`() {
        val fileName = "fileToDelete.txt"

        every { minioClient.removeObject(any<RemoveObjectArgs>()) } returns Unit

        StepVerifier.create(minioService.deleteFile(fileName))
            .verifyComplete()

        verify(exactly = 1) {
            minioClient.removeObject(match<RemoveObjectArgs> {
                it.bucket() == bucketName && it.`object`() == fileName
            })
        }
        verify(exactly = 1) {
            rabbitTemplate.convertAndSend(
                eq("file_exchange"),
                eq("file.deleted"),
                eq(fileName)
            )
        }
    }
}