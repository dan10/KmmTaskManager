package com.danioliveira.taskmanager.domain

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val isDone: Boolean = false,
    val priority: Priority,
    val dueDate: String
)
