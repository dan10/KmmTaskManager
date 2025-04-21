package com.danioliveira.taskmanager.domain

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val googleId: String? = null,
    val createdAt: String
)
