package com.danioliveira.taskmanager.domain

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String,
    val name: String,
    val completed: Int,
    val inProgress: Int,
    val total: Int,
    val description: String?,
)
