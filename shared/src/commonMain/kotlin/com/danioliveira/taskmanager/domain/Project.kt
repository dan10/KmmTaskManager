package com.danioliveira.taskmanager.domain

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val createdAt: String
)
