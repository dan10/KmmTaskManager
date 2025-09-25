package com.danioliveira.taskmanager.domain

import kotlinx.serialization.Serializable

@Serializable
data class ProjectAssignment(
    val id: String,
    val projectId: String,
    val userId: String,
    val assignedAt: String
)
