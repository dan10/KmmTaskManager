package com.danioliveira.taskmanager.api.response

import kotlinx.serialization.Serializable

/**
 * Response model for a file.
 */
@Serializable
data class FileResponse(
    val id: String,
    val name: String,
    val size: String,
    val uploadedDate: String,
    val taskId: String,
    val url: String,
    val contentType: String
)