package com.danioliveira.taskmanager.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import java.util.Base64
import java.util.UUID

/**
 * Interface for interacting with S3-compatible storage.
 */
interface IS3Client {
    /**
     * Uploads a file to S3-compatible storage.
     *
     * @param fileName The name of the file
     * @param contentType The MIME type of the file
     * @param fileBytes The content of the file as ByteArray
     * @return The URL of the uploaded file
     */
    suspend fun uploadFile(fileName: String, contentType: String, fileBytes: ByteArray): String
}

/**
 * Client for interacting with S3-compatible storage (MinIO).
 * This class handles file uploads to MinIO.
 */
class S3Client(
    private val endpoint: String,
    private val accessKey: String,
    private val secretKey: String,
    private val bucket: String,
    private val region: String
) : IS3Client {
    private val httpClient = HttpClient()

    /**
     * Uploads a file to MinIO.
     *
     * @param fileName The name of the file
     * @param contentType The MIME type of the file
     * @param fileBytes The content of the file as ByteArray
     * @return The URL of the uploaded file
     */
    override suspend fun uploadFile(fileName: String, contentType: String, fileBytes: ByteArray): String {
        // Generate a unique file name to avoid collisions
        val uniqueFileName = "${UUID.randomUUID()}-$fileName"

        // Construct the URL for the file in MinIO
        val objectKey = uniqueFileName
        val fileUrl = "$endpoint/$bucket/$objectKey"

        try {
            // Create the bucket if it doesn't exist
            ensureBucketExists()

            // Upload the file to MinIO using a PUT request
            val response = httpClient.put("$endpoint/$bucket/$objectKey") {
                headers {
                    append(HttpHeaders.ContentType, contentType)
                    append(HttpHeaders.ContentLength, fileBytes.size.toString())
                    // Basic authentication for MinIO
                    append(
                        HttpHeaders.Authorization,
                        "Basic ${Base64.getEncoder().encodeToString("$accessKey:$secretKey".toByteArray())}"
                    )
                }
                setBody(fileBytes)
            }

            if (response.status.isSuccess()) {
                println("Successfully uploaded file $uniqueFileName to MinIO")
                return fileUrl
            } else {
                println("Failed to upload file to MinIO: ${response.status}")
                throw Exception("Failed to upload file to MinIO: ${response.status}")
            }
        } catch (e: Exception) {
            println("Error uploading file to MinIO: ${e.message}")
            throw e
        }
    }

    /**
     * Ensures that the bucket exists in MinIO.
     * If the bucket doesn't exist, it creates it.
     */
    private suspend fun ensureBucketExists() {
        try {
            // Check if bucket exists
            val response = httpClient.head("$endpoint/$bucket") {
                headers {
                    // Basic authentication for MinIO
                    append(
                        HttpHeaders.Authorization,
                        "Basic ${Base64.getEncoder().encodeToString("$accessKey:$secretKey".toByteArray())}"
                    )
                }
            }

            if (!response.status.isSuccess()) {
                // Bucket doesn't exist, create it
                val createResponse = httpClient.put("$endpoint/$bucket") {
                    headers {
                        // Basic authentication for MinIO
                        append(
                            HttpHeaders.Authorization,
                            "Basic ${Base64.getEncoder().encodeToString("$accessKey:$secretKey".toByteArray())}"
                        )
                    }
                }

                if (!createResponse.status.isSuccess()) {
                    println("Failed to create bucket: ${createResponse.status}")
                    throw Exception("Failed to create bucket: ${createResponse.status}")
                }
            }
        } catch (e: Exception) {
            println("Error checking/creating bucket: ${e.message}")
            // Continue anyway, as the bucket might already exist
        }
    }
}

/**
 * Factory for creating S3Client instances.
 */
object S3ClientFactory {
    // For testing purposes
    private var mockClient: IS3Client? = null

    /**
     * Creates an S3Client instance from environment variables.
     *
     * @return IS3Client instance
     */
    fun createFromEnv(): IS3Client {
        // If a mock client is set for testing, return it
        mockClient?.let { return it }

        val endpoint = System.getenv("S3_ENDPOINT") ?: "http://minio:9000"
        val accessKey = System.getenv("S3_ACCESS_KEY") ?: "minioadmin"
        val secretKey = System.getenv("S3_SECRET_KEY") ?: "minioadmin"
        val bucket = System.getenv("S3_BUCKET") ?: "taskit-files"
        val region = System.getenv("S3_REGION") ?: "us-east-1"

        return S3Client(endpoint, accessKey, secretKey, bucket, region)
    }

    /**
     * Overrides the S3Client for testing purposes.
     *
     * @param client The mock IS3Client to use
     */
    fun overrideForTesting(client: IS3Client) {
        mockClient = client
    }

    /**
     * Resets the override for testing purposes.
     */
    fun resetOverride() {
        mockClient = null
    }
}
