package com.danioliveira.taskmanager.domain

import kotlinx.serialization.Serializable

@Serializable
data class ProjectInvitation(
    val id: String,
    val projectId: String,
    val invitedUserId: String,
    val inviterId: String,
    val status: String,
    val invitedAt: String
)
