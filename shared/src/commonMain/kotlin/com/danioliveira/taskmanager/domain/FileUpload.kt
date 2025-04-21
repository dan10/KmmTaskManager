package com.danioliveira.taskmanager.domain

import kotlinx.serialization.Serializable

@Serializable
data class FileUpload(
    val id: String,
    val filename: String,
    val uploaderId: String,
    val projectId: String?,
    val taskId: String?,
    val url: String,
    val uploadedAt: String
)
