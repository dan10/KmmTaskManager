package com.danioliveira.taskmanager.domain

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Task(
    val id: Uuid,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: Priority,
    val dueDate: String
)
