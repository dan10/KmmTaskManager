package com.danioliveira.taskmanager.model

data class UserWithPassword(
    val id: String,
    val email: String,
    val displayName: String,
    val googleId: String? = null,
    val createdAt: String,
    val passwordHash: String? = null
)
