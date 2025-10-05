package com.danioliveira.taskmanager.core.domain.model

import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Task(
    val id: Uuid,
    val title: String,
    val description: String,
    val projectName: String?,
    val status: TaskStatus,
    val priority: Priority,
    val dueDate: LocalDateTime?
)
